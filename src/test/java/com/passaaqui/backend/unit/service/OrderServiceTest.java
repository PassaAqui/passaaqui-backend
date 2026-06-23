package com.passaaqui.backend.unit.service;

import com.passaaqui.backend.infra.exception.ConflictException;
import com.passaaqui.backend.infra.exception.InvalidRequestException;
import com.passaaqui.backend.infra.exception.ResourceNotFoundException;
import com.passaaqui.backend.infra.integration.abacatepay.AbacateClient;
import com.passaaqui.backend.infra.integration.abacatepay.dto.CheckoutResponseDTO;
import com.passaaqui.backend.modules.order.dto.CheckoutRequestDTO;
import com.passaaqui.backend.modules.order.model.OrderModel;
import com.passaaqui.backend.modules.order.model.enums.OrderStatus;
import com.passaaqui.backend.modules.order.repository.OrderRepository;
import com.passaaqui.backend.modules.order.service.OrderService;
import com.passaaqui.backend.modules.product.model.ProductModel;
import com.passaaqui.backend.modules.product.repository.ProductRepository;
import com.passaaqui.backend.modules.shopkeeper.model.ShopkeeperModel;
import com.passaaqui.backend.modules.shopkeeper.repository.ShopkeeperRepository;
import com.passaaqui.backend.modules.tourist.model.TouristModel;
import com.passaaqui.backend.modules.tourist.repository.TouristRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ShopkeeperRepository shopkeeperRepository;

    @Mock
    private TouristRepository touristRepository;

    @Mock
    private AbacateClient abacateClient;

    @InjectMocks
    private OrderService orderService;

    private TouristModel tourist;
    private ShopkeeperModel shopkeeper;
    private ProductModel product;
    private OrderModel order;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        var auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn("1");
        var securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);

        tourist = new TouristModel();
        tourist.setId(1);
        tourist.setCurrentXP(1000);

        shopkeeper = new ShopkeeperModel();
        shopkeeper.setId(1);
        shopkeeper.setCompanyName("Test Shop");

        product = new ProductModel();
        product.setId(1);
        product.setName("Test Product");
        product.setPrice(50.0);
        product.setMaxXp(500);
        product.setShopkeeper(shopkeeper);

        order = OrderModel.builder()
                .id(UUID.randomUUID())
                .tourist(tourist)
                .shopkeeper(shopkeeper)
                .product(product)
                .quantity(1)
                .totalAmount(BigDecimal.valueOf(50.0))
                .status(OrderStatus.PENDING)
                .build();

        ReflectionTestUtils.setField(orderService, "xpConversionFactor", 100);
        ReflectionTestUtils.setField(orderService, "xpTakeRate", 0.05);
        ReflectionTestUtils.setField(orderService, "xpMarginFactor", 0.60);
        ReflectionTestUtils.setField(orderService, "xpAbsoluteCeiling", 15.00);
    }

    @Test
    void checkout_shouldCreateOrder_whenValidRequest() {
        var dto = new CheckoutRequestDTO(1, null);
        var checkoutResponse = new CheckoutResponseDTO("tx_123", 5000, "active", false, "pix-code", "qr-base64", 0, null, null, null, "2026-06-23T11:00:00Z", Map.of());

        when(touristRepository.findById(1)).thenReturn(Optional.of(tourist));
        when(orderRepository.existsByTourist_IdAndStatusNotIn(eq(1), anyList())).thenReturn(false);
        when(productRepository.findById(1)).thenReturn(Optional.of(product));
        when(orderRepository.save(any(OrderModel.class))).thenReturn(order);
        when(abacateClient.createCheckout(any())).thenReturn(checkoutResponse);

        var result = orderService.checkout(dto);

        assertNotNull(result);
        assertEquals(product.getName(), result.productName());
        verify(orderRepository, times(2)).save(any(OrderModel.class));
    }

    @Test
    void checkout_shouldApplyXpDiscount_whenValidXpToUse() {
        var dto = new CheckoutRequestDTO(1, 200);
        var checkoutResponse = new CheckoutResponseDTO("tx_123", 5000, "active", false, "pix-code", "qr-base64", 0, null, null, null, "2026-06-23T11:00:00Z", Map.of());
        BigDecimal expectedDiscount = BigDecimal.valueOf(2.00); // 200 / 100
        BigDecimal expectedTotal = BigDecimal.valueOf(50.00).subtract(expectedDiscount);

        when(touristRepository.findById(1)).thenReturn(Optional.of(tourist));
        when(orderRepository.existsByTourist_IdAndStatusNotIn(eq(1), anyList())).thenReturn(false);
        when(productRepository.findById(1)).thenReturn(Optional.of(product));
        when(orderRepository.save(any(OrderModel.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(abacateClient.createCheckout(any())).thenReturn(checkoutResponse);

        var result = orderService.checkout(dto);

        assertNotNull(result);
        assertEquals(0, expectedTotal.compareTo(result.totalAmount()));
        assertEquals(Integer.valueOf(800), tourist.getCurrentXP());
        verify(touristRepository).save(tourist);
    }

    @Test
    void checkout_shouldThrow_whenInsufficientXp() {
        tourist.setCurrentXP(50);
        var dto = new CheckoutRequestDTO(1, 100);

        when(touristRepository.findById(1)).thenReturn(Optional.of(tourist));
        when(orderRepository.existsByTourist_IdAndStatusNotIn(eq(1), anyList())).thenReturn(false);
        when(productRepository.findById(1)).thenReturn(Optional.of(product));

        assertThrows(InvalidRequestException.class, () -> orderService.checkout(dto));
    }

    @Test
    void checkout_shouldThrow_whenXpExceedsProductMax() {
        var dto = new CheckoutRequestDTO(1, 600);

        when(touristRepository.findById(1)).thenReturn(Optional.of(tourist));
        when(orderRepository.existsByTourist_IdAndStatusNotIn(eq(1), anyList())).thenReturn(false);
        when(productRepository.findById(1)).thenReturn(Optional.of(product));

        assertThrows(InvalidRequestException.class, () -> orderService.checkout(dto));
    }

    @Test
    void checkout_shouldNotApplyDiscount_whenXpToUseNull() {
        var dto = new CheckoutRequestDTO(1, null);
        var checkoutResponse = new CheckoutResponseDTO("tx_123", 5000, "active", false, "pix-code", "qr-base64", 0, null, null, null, "2026-06-23T11:00:00Z", Map.of());

        when(touristRepository.findById(1)).thenReturn(Optional.of(tourist));
        when(orderRepository.existsByTourist_IdAndStatusNotIn(eq(1), anyList())).thenReturn(false);
        when(productRepository.findById(1)).thenReturn(Optional.of(product));
        when(orderRepository.save(any(OrderModel.class))).thenReturn(order);
        when(abacateClient.createCheckout(any())).thenReturn(checkoutResponse);

        var result = orderService.checkout(dto);

        assertNotNull(result);
        assertEquals(0, BigDecimal.valueOf(50.0).compareTo(result.totalAmount()));
        assertEquals(Integer.valueOf(1000), tourist.getCurrentXP());
        verify(touristRepository, never()).save(tourist);
    }

    @Test
    void checkout_shouldThrow_whenTouristNotFound() {
        var dto = new CheckoutRequestDTO(1, null);

        when(touristRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> orderService.checkout(dto));
    }

    @Test
    void checkout_shouldThrow_whenActiveOrderExists() {
        var dto = new CheckoutRequestDTO(1, null);

        when(touristRepository.findById(1)).thenReturn(Optional.of(tourist));
        when(orderRepository.existsByTourist_IdAndStatusNotIn(eq(1), anyList())).thenReturn(true);

        assertThrows(ConflictException.class, () -> orderService.checkout(dto));
    }

    @Test
    void checkout_shouldThrow_whenProductNotFound() {
        var dto = new CheckoutRequestDTO(999, null);

        when(touristRepository.findById(1)).thenReturn(Optional.of(tourist));
        when(orderRepository.existsByTourist_IdAndStatusNotIn(eq(1), anyList())).thenReturn(false);
        when(productRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> orderService.checkout(dto));
    }

    @Test
    void getShopkeeperOrders_shouldReturnOrders() {
        when(shopkeeperRepository.findById(1)).thenReturn(Optional.of(shopkeeper));
        when(orderRepository.findByShopkeeper_IdAndStatus(1, OrderStatus.PAID)).thenReturn(List.of(order));

        var result = orderService.getShopkeeperOrders();

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    void getShopkeeperOrders_shouldThrow_whenShopkeeperNotFound() {
        when(shopkeeperRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> orderService.getShopkeeperOrders());
    }

    @Test
    void getMyCurrentOrder_shouldReturnOrder() {
        when(touristRepository.findById(1)).thenReturn(Optional.of(tourist));
        when(orderRepository.findTopByTourist_IdAndStatusOrderByCreatedAtDesc(1, OrderStatus.PAID))
                .thenReturn(Optional.of(order));

        var result = orderService.getMyCurrentOrder();

        assertNotNull(result);
        assertEquals(order.getId(), result.id());
    }

    @Test
    void getMyCurrentOrder_shouldThrow_whenTouristNotFound() {
        when(touristRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> orderService.getMyCurrentOrder());
    }

    @Test
    void getShopkeeperHistory_shouldReturnAllOrders() {
        when(shopkeeperRepository.findById(1)).thenReturn(Optional.of(shopkeeper));
        when(orderRepository.findByShopkeeper_IdOrderByCreatedAtDesc(1)).thenReturn(List.of(order));

        var result = orderService.getShopkeeperHistory();

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(order.getId(), result.get(0).id());
    }

    @Test
    void getShopkeeperHistory_shouldThrow_whenShopkeeperNotFound() {
        when(shopkeeperRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> orderService.getShopkeeperHistory());
    }

    @Test
    void getTouristHistory_shouldReturnAllOrders() {
        when(touristRepository.findById(1)).thenReturn(Optional.of(tourist));
        when(orderRepository.findByTourist_IdOrderByCreatedAtDesc(1)).thenReturn(List.of(order));

        var result = orderService.getTouristHistory();

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(order.getId(), result.get(0).id());
    }

    @Test
    void getTouristHistory_shouldThrow_whenTouristNotFound() {
        when(touristRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> orderService.getTouristHistory());
    }

    @Test
    void getMyCurrentOrder_shouldThrow_whenNoPaidOrder() {
        when(touristRepository.findById(1)).thenReturn(Optional.of(tourist));
        when(orderRepository.findTopByTourist_IdAndStatusOrderByCreatedAtDesc(1, OrderStatus.PAID))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> orderService.getMyCurrentOrder());
    }
}
