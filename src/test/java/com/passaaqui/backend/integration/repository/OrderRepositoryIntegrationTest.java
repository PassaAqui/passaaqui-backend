package com.passaaqui.backend.integration.repository;

import com.passaaqui.backend.infra.integration.abacatepay.AbacateClient;
import com.passaaqui.backend.infra.integration.abacatepay.dto.CheckoutResponseDTO;
import com.passaaqui.backend.modules.category.model.CategoryModel;
import com.passaaqui.backend.modules.category.repository.CategoryRepository;
import com.passaaqui.backend.modules.order.model.OrderModel;
import com.passaaqui.backend.modules.order.model.enums.OrderStatus;
import com.passaaqui.backend.modules.order.repository.OrderRepository;
import com.passaaqui.backend.modules.product.model.ProductModel;
import com.passaaqui.backend.modules.product.repository.ProductRepository;
import com.passaaqui.backend.modules.shopkeeper.model.ShopkeeperModel;
import com.passaaqui.backend.modules.shopkeeper.repository.ShopkeeperRepository;
import com.passaaqui.backend.modules.tourist.model.TouristModel;
import com.passaaqui.backend.modules.tourist.repository.TouristRepository;
import com.passaaqui.backend.modules.user.model.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@Import(OrderRepositoryIntegrationTest.TestJpaConfig.class)
class OrderRepositoryIntegrationTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private OrderRepository orderRepository;

    private TouristModel tourist;
    private ShopkeeperModel shopkeeper;
    private ProductModel product;

    static class TestJpaConfig {
    }

    @BeforeEach
    void setUp() {
        var category = new CategoryModel();
        category.setName("Test Category");
        category = em.persistAndFlush(category);

        tourist = new TouristModel();
        tourist.setEmail("tourist@test.com");
        tourist.setPassword("encoded-pass");
        tourist.setName("Test Tourist");
        tourist.setRole(UserRole.TOURIST);
        tourist.setDocumentId("52998224725");
        tourist = em.persistAndFlush(tourist);

        shopkeeper = new ShopkeeperModel();
        shopkeeper.setEmail("shop@test.com");
        shopkeeper.setPassword("encoded-pass");
        shopkeeper.setName("Test Shop");
        shopkeeper.setRole(UserRole.SHOPKEEPER);
        shopkeeper.setDocumentId("11222333000181");
        shopkeeper.setCompanyName("Test Company");
        shopkeeper.setCategory(category);
        shopkeeper = em.persistAndFlush(shopkeeper);

        product = new ProductModel();
        product.setName("Test Product");
        product.setPrice(50.0);
        product.setShopkeeper(shopkeeper);
        product.setCategory(category);
        product = em.persistAndFlush(product);
    }

    private OrderModel createOrder(OrderStatus status) {
        var order = OrderModel.builder()
                .tourist(tourist)
                .shopkeeper(shopkeeper)
                .product(product)
                .quantity(1)
                .totalAmount(BigDecimal.valueOf(50.0))
                .status(status)
                .build();
        return em.persistAndFlush(order);
    }

    @Test
    void shouldSaveAndFindById() {
        var order = createOrder(OrderStatus.PENDING);

        var found = orderRepository.findById(order.getId());

        assertTrue(found.isPresent());
        assertEquals(OrderStatus.PENDING, found.get().getStatus());
        assertEquals(tourist.getId(), found.get().getTourist().getId());
    }

    @Test
    void shouldFindByTransactionId() {
        var order = createOrder(OrderStatus.AWAITING_PAYMENT);
        order.setTransactionId("tx_test_123");
        em.persistAndFlush(order);

        var found = orderRepository.findByTransactionId("tx_test_123");

        assertTrue(found.isPresent());
        assertEquals(order.getId(), found.get().getId());
    }

    @Test
    void shouldFindByShopkeeperIdAndStatus() {
        createOrder(OrderStatus.PAID);
        createOrder(OrderStatus.PAID);
        createOrder(OrderStatus.CANCELED);

        var paidOrders = orderRepository.findByShopkeeper_IdAndStatus(shopkeeper.getId(), OrderStatus.PAID);

        assertEquals(2, paidOrders.size());
    }

    @Test
    void shouldFindTopByTouristIdAndStatus() {
        createOrder(OrderStatus.PAID);

        var found = orderRepository.findTopByTourist_IdAndStatusOrderByCreatedAtDesc(
                tourist.getId(), OrderStatus.PAID);

        assertTrue(found.isPresent());
        assertEquals(tourist.getId(), found.get().getTourist().getId());
    }

    @Test
    void shouldCheckActiveOrderExistsByTourist() {
        createOrder(OrderStatus.AWAITING_PAYMENT);

        var exists = orderRepository.existsByTourist_IdAndStatusNotIn(
                tourist.getId(), List.of(OrderStatus.COMPLETED, OrderStatus.CANCELED));

        assertTrue(exists);
    }

    @Test
    void shouldReturnFalseWhenNoActiveOrder() {
        createOrder(OrderStatus.COMPLETED);

        var exists = orderRepository.existsByTourist_IdAndStatusNotIn(
                tourist.getId(), List.of(OrderStatus.COMPLETED, OrderStatus.CANCELED));

        assertFalse(exists);
    }

    @Test
    void shouldFindByStatusAndCreatedAtBefore() {
        var order = createOrder(OrderStatus.AWAITING_PAYMENT);
        em.clear();
        em.getEntityManager().createNativeQuery(
                "UPDATE tb_orders SET created_at = ? WHERE id = ?")
                .setParameter(1, LocalDateTime.now().minusHours(2))
                .setParameter(2, order.getId().toString())
                .executeUpdate();
        em.clear();

        var expired = orderRepository.findByStatusAndCreatedAtBefore(
                OrderStatus.AWAITING_PAYMENT, LocalDateTime.now().minusHours(1));

        assertFalse(expired.isEmpty());
        assertEquals(order.getId(), expired.get(0).getId());
    }

    @Test
    void shouldReturnEmpty_whenNoMatchingTransaction() {
        var found = orderRepository.findByTransactionId("nonexistent");

        assertTrue(found.isEmpty());
    }
}
