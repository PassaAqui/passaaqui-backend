package com.passaaqui.backend.modules.order.service;

import com.passaaqui.backend.infra.exception.ResourceNotFoundException;
import com.passaaqui.backend.modules.order.dto.CheckoutRequestDTO;
import com.passaaqui.backend.modules.order.dto.OrderResponseDTO;
import com.passaaqui.backend.modules.order.model.OrderModel;
import com.passaaqui.backend.modules.order.model.enums.OrderStatus;
import com.passaaqui.backend.modules.order.repository.OrderRepository;
import com.passaaqui.backend.modules.product.model.ProductModel;
import com.passaaqui.backend.modules.product.repository.ProductRepository;
import com.passaaqui.backend.modules.tourist.model.TouristModel;
import com.passaaqui.backend.modules.tourist.repository.TouristRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final TouristRepository touristRepository;

    @Transactional
    public OrderResponseDTO checkout(CheckoutRequestDTO request) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        TouristModel tourist = touristRepository.findById(Integer.parseInt(userId))
                .orElseThrow(() -> new ResourceNotFoundException("Tourist not found"));

        ProductModel product = productRepository.findById(request.productId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + request.productId()));

        BigDecimal unitPrice = BigDecimal.valueOf(product.getPrice());
        BigDecimal totalAmount = unitPrice.multiply(BigDecimal.valueOf(request.quantity()));
        String transactionId = UUID.randomUUID().toString();

        OrderModel order = OrderModel.builder()
                .tourist(tourist)
                .shopkeeper(product.getShopkeeper())
                .product(product)
                .quantity(request.quantity())
                .totalAmount(totalAmount)
                .status(OrderStatus.PENDING)
                .transactionId(transactionId)
                .build();

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
                order.getCreatedAt()
        );
    }
}
