package com.passaaqui.backend.modules.order.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.passaaqui.backend.infra.integration.abacatepay.AbacateClient;
import com.passaaqui.backend.modules.order.dto.OrderStatusDTO;
import com.passaaqui.backend.modules.order.model.OrderModel;
import com.passaaqui.backend.modules.order.model.enums.OrderStatus;
import com.passaaqui.backend.modules.order.repository.OrderRepository;
import com.passaaqui.backend.modules.product.model.ProductModel;
import com.passaaqui.backend.modules.product.repository.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders-webhook")
@RequiredArgsConstructor
public class OrderWebhookController {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final ObjectMapper objectMapper;
    private final AbacateClient abacateClient;
    private final SimpMessagingTemplate messagingTemplate;

    @Value("${abacatepay.webhook.secret:}")
    private String webhookSecret;

    @Transactional
    @PostMapping("/webhook/abacatepay")
    public ResponseEntity<Void> handleWebhook(
            @RequestHeader("X-Webhook-Signature") String signature,
            @RequestBody String rawBody) {

        if (!isSignatureValid(rawBody, signature)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            WebhookPayload payload = objectMapper.readValue(rawBody, WebhookPayload.class);

            if (payload.event() == null || payload.data() == null || payload.data().transparent() == null) {
                return ResponseEntity.ok().build();
            }

            String externalId = payload.data().transparent().externalId();
            if (externalId == null) {
                return ResponseEntity.ok().build();
            }

            orderRepository.findByIdForUpdate(UUID.fromString(externalId)).ifPresent(order -> {
                OrderStatus currentStatus = order.getStatus();
                switch (payload.event()) {
                    case "transparent.completed" -> {
                        if (currentStatus == OrderStatus.AWAITING_PAYMENT || currentStatus == OrderStatus.PENDING) {
                            order.setStatus(OrderStatus.PAID);
                            order.setPickupCode(generatePickupCode());
                            orderRepository.save(order);
                            messagingTemplate.convertAndSend(
                                    "/topic/orders/" + order.getId(),
                                    new OrderStatusDTO(order.getId(), order.getStatus(), order.getPickupCode())
                            );
                        }
                    }
                    case "transparent.canceled", "transparent.failed", "transparent.refunded" -> {
                        if (currentStatus != OrderStatus.CANCELED && currentStatus != OrderStatus.PAID && currentStatus != OrderStatus.COMPLETED) {
                            order.setStatus(OrderStatus.CANCELED);
                            restoreStock(order);
                            orderRepository.save(order);
                            messagingTemplate.convertAndSend(
                                    "/topic/orders/" + order.getId(),
                                    new OrderStatusDTO(order.getId(), order.getStatus(), order.getPickupCode())
                            );
                        }
                    }
                }
            });

        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok().build();
    }

    @Transactional
    @Scheduled(cron = "0 * * * * *")
    public void expireOrdersJob() {
        LocalDateTime limitTime = LocalDateTime.now().minusMinutes(5);
        List<OrderModel> orders = orderRepository.findByStatusAndCreatedAtBefore(OrderStatus.AWAITING_PAYMENT, limitTime);

        for (OrderModel o : orders) {
            OrderModel order = orderRepository.findByIdForUpdate(o.getId()).orElse(null);
            if (order == null || order.getStatus() != OrderStatus.AWAITING_PAYMENT) {
                continue;
            }
            order.setStatus(OrderStatus.CANCELED);
            restoreStock(order);
            orderRepository.save(order);
            messagingTemplate.convertAndSend(
                    "/topic/orders/" + order.getId(),
                    new OrderStatusDTO(order.getId(), order.getStatus(), order.getPickupCode())
            );
        }
    }

    @Transactional
    @Scheduled(cron = "30 * * * * *")
    public void reconcileOrdersJob() {
        LocalDateTime limitTime = LocalDateTime.now().minusMinutes(5);
        List<OrderModel> orders = orderRepository.findByStatusAndCreatedAtBefore(OrderStatus.AWAITING_PAYMENT, limitTime);

        for (OrderModel o : orders) {
            try {
                OrderModel order = orderRepository.findByIdForUpdate(o.getId()).orElse(null);
                if (order == null || order.getStatus() != OrderStatus.AWAITING_PAYMENT) {
                    continue;
                }
                String gatewayStatus = abacateClient.checkPaymentStatus(order.getTransactionId());
                if ("PAID".equals(gatewayStatus)) {
                    order.setStatus(OrderStatus.PAID);
                    order.setPickupCode(generatePickupCode());
                    orderRepository.save(order);
                    messagingTemplate.convertAndSend(
                            "/topic/orders/" + order.getId(),
                            new OrderStatusDTO(order.getId(), order.getStatus(), order.getPickupCode())
                    );
                }
            } catch (Exception ignored) {
            }
        }
    }

    private boolean isSignatureValid(String rawBody, String signature) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(webhookSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKey);
            byte[] hash = mac.doFinal(rawBody.getBytes(StandardCharsets.UTF_8));
            String expectedSignature = Base64.getEncoder().encodeToString(hash);

            return expectedSignature.equals(signature);
        } catch (Exception e) {
            return false;
        }
    }

    private String generatePickupCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(6);
        for (int i = 0; i < 6; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    private void restoreStock(OrderModel order) {
        if (order.getProduct() == null || order.getProduct().getId() == null) {
            return;
        }
        ProductModel product = productRepository.findByIdForUpdate(order.getProduct().getId())
                .orElse(order.getProduct());
        product.setStock(product.getStock() + (order.getQuantity() != null ? order.getQuantity() : 1));
        productRepository.save(product);
    }

    public record WebhookPayload(String event, WebhookData data) {}
    public record WebhookData(WebhookTransparent transparent) {}
    public record WebhookTransparent(String externalId, String status) {}
}