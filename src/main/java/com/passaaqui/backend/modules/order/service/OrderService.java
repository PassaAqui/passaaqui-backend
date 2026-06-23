package com.passaaqui.backend.modules.order.service;

import com.passaaqui.backend.infra.integration.abacatepay.AbacateClient;
import com.passaaqui.backend.infra.exception.ConflictException;
import com.passaaqui.backend.infra.exception.ResourceNotFoundException;
import com.passaaqui.backend.modules.order.dto.CheckoutRequestDTO;
import com.passaaqui.backend.modules.order.dto.OrderResponseDTO;
import com.passaaqui.backend.modules.order.model.OrderModel;
import com.passaaqui.backend.modules.order.model.enums.OrderStatus;
import com.passaaqui.backend.modules.order.repository.OrderRepository;
import com.passaaqui.backend.modules.product.model.ProductModel;
import com.passaaqui.backend.modules.product.repository.ProductRepository;
import com.passaaqui.backend.modules.shopkeeper.model.ShopkeeperModel;
import com.passaaqui.backend.modules.shopkeeper.repository.ShopkeeperRepository;
import com.passaaqui.backend.modules.tourist.model.TouristModel;
import com.passaaqui.backend.modules.tourist.repository.TouristRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final ShopkeeperRepository shopkeeperRepository;
    private final TouristRepository touristRepository;
    private final AbacateClient abacateClient;

    @Transactional
    public OrderResponseDTO checkout(CheckoutRequestDTO request) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        TouristModel tourist = touristRepository.findById(Integer.parseInt(userId))
                .orElseThrow(() -> new ResourceNotFoundException("Tourist not found"));

        if (orderRepository.existsByTourist_IdAndStatusNotIn(tourist.getId(), List.of(OrderStatus.COMPLETED, OrderStatus.CANCELED))) {
            throw new ConflictException("Você já possui um pedido ativo. Finalize-o antes de comprar outro.");
        }

        ProductModel product = productRepository.findById(request.productId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + request.productId()));

        BigDecimal unitPrice = BigDecimal.valueOf(product.getPrice());
        BigDecimal totalAmount = unitPrice;

        OrderModel order = OrderModel.builder()
                .tourist(tourist)
                .shopkeeper(product.getShopkeeper())
                .product(product)
                .quantity(1)
                .totalAmount(totalAmount)
                .status(OrderStatus.PENDING)
                .build();

        order = orderRepository.save(order);

        var checkoutRequest = new com.passaaqui.backend.infra.integration.abacatepay.dto.CheckoutRequestDTO(
                totalAmount.doubleValue(), order.getId()
        );
        var checkoutResponse = abacateClient.createCheckout(checkoutRequest);

        order.setTransactionId(checkoutResponse.id());
        order.setPix(checkoutResponse.brCode());
        order.setQrCodeUrl(checkoutResponse.brCodeBase64());
        order.setStatus(OrderStatus.AWAITING_PAYMENT);

        if (checkoutResponse.expiresAt() != null) {
            order.setPixExpiresAt(LocalDateTime.ofInstant(Instant.parse(checkoutResponse.expiresAt()), ZoneId.systemDefault()));
        }

        order = orderRepository.save(order);

        return new OrderResponseDTO(
                order.getId(),
                order.getProduct().getId(),
                order.getProduct().getName(),
                order.getShopkeeper().getId(),
                order.getShopkeeper().getCompanyName(),
                order.getQuantity(),
                unitPrice,
                order.getTotalAmount(),
                order.getStatus(),
                order.getTransactionId(),
                order.getCreatedAt(),
                order.getPix(),
                order.getQrCodeUrl(),
                order.getPixExpiresAt(),
                null
        );
    }

    public List<OrderResponseDTO> getShopkeeperOrders() {
        String userId = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        ShopkeeperModel shopkeeper = shopkeeperRepository.findById(Integer.parseInt(userId))
                .orElseThrow(() -> new ResourceNotFoundException("Shopkeeper not found"));

        return orderRepository.findByShopkeeper_IdAndStatus(shopkeeper.getId(), OrderStatus.PAID)
                .stream()
                .map(order -> new OrderResponseDTO(
                        order.getId(),
                        order.getProduct().getId(),
                        order.getProduct().getName(),
                        order.getShopkeeper().getId(),
                        order.getShopkeeper().getCompanyName(),
                        order.getQuantity(),
                        BigDecimal.valueOf(order.getProduct().getPrice()),
                        order.getTotalAmount(),
                        order.getStatus(),
                        order.getTransactionId(),
                        order.getCreatedAt(),
                        order.getPix(),
                        order.getQrCodeUrl(),
                        order.getPixExpiresAt(),
                        order.getPickupCode()
                ))
                .toList();
    }

    public OrderResponseDTO getMyCurrentOrder() {
        String userId = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        TouristModel tourist = touristRepository.findById(Integer.parseInt(userId))
                .orElseThrow(() -> new ResourceNotFoundException("Tourist not found"));

        OrderModel order = orderRepository.findTopByTourist_IdAndStatusOrderByCreatedAtDesc(tourist.getId(), OrderStatus.PAID)
                .orElseThrow(() -> new ResourceNotFoundException("No paid order found"));

        return new OrderResponseDTO(
                order.getId(),
                order.getProduct().getId(),
                order.getProduct().getName(),
                order.getShopkeeper().getId(),
                order.getShopkeeper().getCompanyName(),
                order.getQuantity(),
                BigDecimal.valueOf(order.getProduct().getPrice()),
                order.getTotalAmount(),
                order.getStatus(),
                order.getTransactionId(),
                order.getCreatedAt(),
                order.getPix(),
                order.getQrCodeUrl(),
                order.getPixExpiresAt(),
                order.getPickupCode()
        );
    }
}
