package com.passaaqui.backend.modules.order.service;

import com.passaaqui.backend.infra.exception.ForbiddenException;
import com.passaaqui.backend.infra.exception.InvalidRequestException;
import com.passaaqui.backend.infra.integration.abacatepay.AbacateClient;
import com.passaaqui.backend.infra.integration.storage.StorageService;
import com.passaaqui.backend.infra.exception.ConflictException;
import com.passaaqui.backend.infra.exception.ResourceNotFoundException;
import com.passaaqui.backend.modules.order.dto.CheckoutRequestDTO;
import com.passaaqui.backend.modules.order.dto.OrderResponseDTO;
import com.passaaqui.backend.modules.order.dto.ShopkeeperOrderDTO;
import com.passaaqui.backend.modules.order.dto.UpdateOrderStatusDTO;
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
import java.security.SecureRandom;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final ShopkeeperRepository shopkeeperRepository;
    private final TouristRepository touristRepository;
    private final AbacateClient abacateClient;
    private final StorageService storageService;

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
        BigDecimal cashDiscount = BigDecimal.ZERO;

        if (request.xpToUse() != null && request.xpToUse() > 0) {
            if (request.xpToUse() > tourist.getCurrentXP()) {
                throw new InvalidRequestException("Saldo de XP insuficiente. Você possui " + tourist.getCurrentXP() + " XP.");
            }

            if (request.xpToUse() > product.getMaxXp()) {
                throw new InvalidRequestException("Este produto permite no máximo " + product.getMaxXp() + " XP de desconto.");
            }

            cashDiscount = calculateDiscount(request.xpToUse(), unitPrice, product.getMaxXp());

            totalAmount = unitPrice.subtract(cashDiscount).max(BigDecimal.ZERO);

            tourist.setCurrentXP(tourist.getCurrentXP() - request.xpToUse());
            touristRepository.save(tourist);
        }

        OrderModel order = OrderModel.builder()
                .tourist(tourist)
                .shopkeeper(product.getShopkeeper())
                .product(product)
                .quantity(1)
                .totalAmount(totalAmount)
                .cashDiscount(cashDiscount)
                .status(OrderStatus.PENDING)
                .code(generateOrderCode())
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

    public List<ShopkeeperOrderDTO> getShopkeeperOrdersByStatus(OrderStatus status) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        ShopkeeperModel shopkeeper = shopkeeperRepository.findById(Integer.parseInt(userId))
                .orElseThrow(() -> new ResourceNotFoundException("Shopkeeper not found"));

        List<OrderModel> orders;
        if (status != null) {
            orders = orderRepository.findByShopkeeper_IdAndStatusOrderByCreatedAtDesc(shopkeeper.getId(), status);
        } else {
            orders = orderRepository.findByShopkeeper_IdOrderByCreatedAtDesc(shopkeeper.getId());
        }

        return orders.stream().map(ShopkeeperOrderDTO::from).toList();
    }

    @Transactional
    public ShopkeeperOrderDTO updateOrderStatus(UUID orderId, UpdateOrderStatusDTO dto) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        ShopkeeperModel shopkeeper = shopkeeperRepository.findById(Integer.parseInt(userId))
                .orElseThrow(() -> new ResourceNotFoundException("Shopkeeper not found"));

        OrderModel order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (!order.getShopkeeper().getId().equals(shopkeeper.getId())) {
            throw new com.passaaqui.backend.infra.exception.ForbiddenException("This order does not belong to you");
        }

        order.setStatus(dto.status());
        order = orderRepository.save(order);

        String productImage = order.getProduct().getImages().isEmpty() ? null
                : storageService.getFileUrl(order.getProduct().getImages().get(0));

        return ShopkeeperOrderDTO.from(order, productImage);
    }

    public List<ShopkeeperOrderDTO> getRecentOrders(Integer shopkeeperId, int limit) {
        return orderRepository.findTop5ByShopkeeper_IdOrderByCreatedAtDesc(shopkeeperId)
                .stream()
                .map(ShopkeeperOrderDTO::from)
                .toList();
    }

    public long countOrdersToday(Integer shopkeeperId) {
        LocalDate today = LocalDate.now();
        return orderRepository.countByShopkeeper_IdAndCreatedAtBetween(shopkeeperId,
                today.atStartOfDay(), today.atTime(LocalTime.MAX));
    }

    public BigDecimal revenueToday(Integer shopkeeperId) {
        LocalDate today = LocalDate.now();
        List<OrderModel> orders = orderRepository.findByShopkeeper_IdOrderByCreatedAtDesc(shopkeeperId);
        return orders.stream()
                .filter(o -> o.getStatus() == OrderStatus.COMPLETED && o.getCreatedAt().toLocalDate().equals(today))
                .map(OrderModel::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public long countPendingOrders(Integer shopkeeperId) {
        return orderRepository.findByShopkeeper_IdAndStatus(shopkeeperId, OrderStatus.PENDING).size();
    }

    public List<BigDecimal> weeklySales(Integer shopkeeperId) {
        LocalDate today = LocalDate.now();
        LocalDate monday = today.with(java.time.DayOfWeek.MONDAY);
        LocalDate sunday = today.with(java.time.DayOfWeek.SUNDAY);

        Map<Integer, BigDecimal> results = orderRepository.findByShopkeeper_IdOrderByCreatedAtDesc(shopkeeperId)
                .stream()
                .filter(o -> !o.getCreatedAt().toLocalDate().isBefore(monday) && !o.getCreatedAt().toLocalDate().isAfter(sunday))
                .filter(o -> o.getStatus() == OrderStatus.COMPLETED || o.getStatus() == OrderStatus.PAID)
                .collect(java.util.stream.Collectors.groupingBy(
                    o -> o.getCreatedAt().getDayOfWeek().getValue(),
                    java.util.stream.Collectors.reducing(BigDecimal.ZERO, OrderModel::getTotalAmount, BigDecimal::add)
                ));

        List<BigDecimal> weeklySales = new ArrayList<>();
        for (int day = 1; day <= 7; day++) {
            weeklySales.add(results.getOrDefault(day, BigDecimal.ZERO));
        }
        return weeklySales;
    }

    public String generateOrderCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(5);
        for (int i = 0; i < 5; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return "#" + sb.toString();
    }

    public OrderResponseDTO findById(UUID id) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = auth.getPrincipal().toString();

        OrderModel order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        var authorities = auth.getAuthorities();
        boolean isAdmin = authorities != null && authorities.stream()
                .anyMatch(a -> a.getAuthority().startsWith("ROLE_ADMIN"));

        if (!isAdmin) {
            Integer currentUserId = Integer.parseInt(userId);
            boolean isTourist = order.getTourist().getId().equals(currentUserId);
            boolean isShopkeeper = order.getShopkeeper().getId().equals(currentUserId);

            if (!isTourist && !isShopkeeper) {
                throw new ForbiddenException("Você não tem permissão para acessar este pedido");
            }
        }

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
