package com.passaaqui.backend.modules.order.service;

import com.passaaqui.backend.infra.exception.InvalidRequestException;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final ShopkeeperRepository shopkeeperRepository;
    private final TouristRepository touristRepository;
    private final AbacateClient abacateClient;

    @Value("${xp.conversion-factor}")
    private int xpConversionFactor;

    @Value("${xp.take-rate}")
    private double xpTakeRate;

    @Value("${xp.margin-factor}")
    private double xpMarginFactor;

    @Value("${xp.absolute-ceiling}")
    private double xpAbsoluteCeiling;

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

        if (product.getStock() < 1) {
            throw new InvalidRequestException("Produto sem estoque disponível.");
        }

        product.setStock(product.getStock() - 1);
        product = productRepository.save(product);

        BigDecimal unitPrice = BigDecimal.valueOf(product.getPrice());
        BigDecimal totalAmount = unitPrice;

        if (request.xpToUse() != null && request.xpToUse() > 0) {
            if (request.xpToUse() > tourist.getCurrentXP()) {
                throw new InvalidRequestException("Saldo de XP insuficiente. Você possui " + tourist.getCurrentXP() + " XP.");
            }

            if (request.xpToUse() > product.getMaxXp()) {
                throw new InvalidRequestException("Este produto permite no máximo " + product.getMaxXp() + " XP de desconto.");
            }

            BigDecimal discountAmount = calculateDiscount(request.xpToUse(), unitPrice, product.getMaxXp());

            totalAmount = unitPrice.subtract(discountAmount).max(BigDecimal.ZERO);

            tourist.setCurrentXP(tourist.getCurrentXP() - request.xpToUse());
            touristRepository.save(tourist);
        }

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

    private BigDecimal calculateDiscount(int xpToUse, BigDecimal unitPrice, int maxXp) {
        BigDecimal baseDiscount = BigDecimal.valueOf(xpToUse)
                .divide(BigDecimal.valueOf(xpConversionFactor), 2, RoundingMode.HALF_DOWN);

        BigDecimal maxXpDiscount = BigDecimal.valueOf(maxXp)
                .divide(BigDecimal.valueOf(xpConversionFactor), 2, RoundingMode.HALF_DOWN);

        BigDecimal ceiling = BigDecimal.valueOf(xpAbsoluteCeiling);

        BigDecimal divisor = BigDecimal.ONE.subtract(BigDecimal.valueOf(xpTakeRate));
        BigDecimal marginRatio = BigDecimal.valueOf(xpMarginFactor).divide(divisor, 10, RoundingMode.HALF_UP);
        BigDecimal maxSafeDiscount = unitPrice.multiply(BigDecimal.ONE.subtract(marginRatio)).max(BigDecimal.ZERO);

        return Stream.of(baseDiscount, maxXpDiscount, ceiling, maxSafeDiscount)
                .min(Comparator.naturalOrder())
                .orElse(BigDecimal.ZERO)
                .max(BigDecimal.ZERO);
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

    public List<OrderResponseDTO> getShopkeeperHistory() {
        String userId = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        ShopkeeperModel shopkeeper = shopkeeperRepository.findById(Integer.parseInt(userId))
                .orElseThrow(() -> new ResourceNotFoundException("Shopkeeper not found"));

        return orderRepository.findByShopkeeper_IdOrderByCreatedAtDesc(shopkeeper.getId())
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

    public List<OrderResponseDTO> getTouristHistory() {
        String userId = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        TouristModel tourist = touristRepository.findById(Integer.parseInt(userId))
                .orElseThrow(() -> new ResourceNotFoundException("Tourist not found"));

        return orderRepository.findByTourist_IdOrderByCreatedAtDesc(tourist.getId())
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
