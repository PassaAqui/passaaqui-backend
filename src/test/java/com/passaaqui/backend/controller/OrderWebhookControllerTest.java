package com.passaaqui.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.passaaqui.backend.infra.abacatepay.AbacateClient;
import com.passaaqui.backend.infra.exception.GlobalExceptionHandler;
import com.passaaqui.backend.modules.order.controller.OrderWebhookController;
import com.passaaqui.backend.modules.order.model.OrderModel;
import com.passaaqui.backend.modules.order.model.enums.OrderStatus;
import com.passaaqui.backend.modules.order.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class OrderWebhookControllerTest {

    private MockMvc mockMvc;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private AbacateClient abacateClient;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private OrderWebhookController controller;

    private static final String WEBHOOK_SECRET = "test-secret-key-12345";

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        ReflectionTestUtils.setField(controller, "webhookSecret", WEBHOOK_SECRET);
    }

    @Test
    void handleWebhook_shouldReturn403WhenInvalidSignature() throws Exception {
        mockMvc.perform(post("/api/orders-webhook/webhook/abacatepay")
                        .header("X-Webhook-Signature", "invalid-signature")
                        .content("{\"event\":\"transparent.completed\",\"data\":{\"transparent\":{\"externalId\":\"any\",\"status\":\"PAID\"}}}")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        verifyNoInteractions(orderRepository);
    }

    @Test
    void handleWebhook_shouldReturn200WhenEventIsNull() throws Exception {
        String payload = "{\"data\":{\"transparent\":{\"externalId\":\"any\",\"status\":\"PAID\"}}}";
        String signature = computeHmacSha256(payload, WEBHOOK_SECRET);

        when(objectMapper.readValue(payload, OrderWebhookController.WebhookPayload.class))
                .thenReturn(new OrderWebhookController.WebhookPayload(null, null));

        mockMvc.perform(post("/api/orders-webhook/webhook/abacatepay")
                        .header("X-Webhook-Signature", signature)
                        .content(payload)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verifyNoInteractions(orderRepository);
    }

    @Test
    void handleWebhook_shouldReturn200WhenExternalIdIsNull() throws Exception {
        String payload = "{\"event\":\"transparent.completed\",\"data\":{\"transparent\":{\"externalId\":null,\"status\":\"PAID\"}}}";
        String signature = computeHmacSha256(payload, WEBHOOK_SECRET);

        when(objectMapper.readValue(payload, OrderWebhookController.WebhookPayload.class))
                .thenReturn(new OrderWebhookController.WebhookPayload(
                        "transparent.completed",
                        new OrderWebhookController.WebhookData(
                                new OrderWebhookController.WebhookTransparent(null, "PAID")
                        )
                ));

        mockMvc.perform(post("/api/orders-webhook/webhook/abacatepay")
                        .header("X-Webhook-Signature", signature)
                        .content(payload)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verifyNoInteractions(orderRepository);
    }

    @Test
    void handleWebhook_shouldProcessCompletedPayment() throws Exception {
        UUID orderId = UUID.randomUUID();
        String payload = "{\"event\":\"transparent.completed\",\"data\":{\"transparent\":{\"externalId\":\"" + orderId + "\",\"status\":\"PAID\"}}}";
        String signature = computeHmacSha256(payload, WEBHOOK_SECRET);

        OrderModel order = OrderModel.builder()
                .id(orderId)
                .status(OrderStatus.AWAITING_PAYMENT)
                .build();

        when(objectMapper.readValue(payload, OrderWebhookController.WebhookPayload.class))
                .thenReturn(new OrderWebhookController.WebhookPayload(
                        "transparent.completed",
                        new OrderWebhookController.WebhookData(
                                new OrderWebhookController.WebhookTransparent(orderId.toString(), "PAID")
                        )
                ));
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        mockMvc.perform(post("/api/orders-webhook/webhook/abacatepay")
                        .header("X-Webhook-Signature", signature)
                        .content(payload)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(orderRepository).save(order);
        verify(messagingTemplate).convertAndSend(eq("/topic/orders/" + orderId), any());
    }

    @Test
    void handleWebhook_shouldProcessCanceledPayment() throws Exception {
        UUID orderId = UUID.randomUUID();
        String payload = "{\"event\":\"transparent.canceled\",\"data\":{\"transparent\":{\"externalId\":\"" + orderId + "\",\"status\":\"CANCELED\"}}}";
        String signature = computeHmacSha256(payload, WEBHOOK_SECRET);

        OrderModel order = OrderModel.builder()
                .id(orderId)
                .status(OrderStatus.AWAITING_PAYMENT)
                .build();

        when(objectMapper.readValue(payload, OrderWebhookController.WebhookPayload.class))
                .thenReturn(new OrderWebhookController.WebhookPayload(
                        "transparent.canceled",
                        new OrderWebhookController.WebhookData(
                                new OrderWebhookController.WebhookTransparent(orderId.toString(), "CANCELED")
                        )
                ));
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        mockMvc.perform(post("/api/orders-webhook/webhook/abacatepay")
                        .header("X-Webhook-Signature", signature)
                        .content(payload)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(orderRepository).save(order);
        verify(messagingTemplate).convertAndSend(eq("/topic/orders/" + orderId), any());
    }

    @Test
    void handleWebhook_shouldProcessFailedPayment() throws Exception {
        UUID orderId = UUID.randomUUID();
        String payload = "{\"event\":\"transparent.failed\",\"data\":{\"transparent\":{\"externalId\":\"" + orderId + "\",\"status\":\"FAILED\"}}}";
        String signature = computeHmacSha256(payload, WEBHOOK_SECRET);

        OrderModel order = OrderModel.builder()
                .id(orderId)
                .status(OrderStatus.AWAITING_PAYMENT)
                .build();

        when(objectMapper.readValue(payload, OrderWebhookController.WebhookPayload.class))
                .thenReturn(new OrderWebhookController.WebhookPayload(
                        "transparent.failed",
                        new OrderWebhookController.WebhookData(
                                new OrderWebhookController.WebhookTransparent(orderId.toString(), "FAILED")
                        )
                ));
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        mockMvc.perform(post("/api/orders-webhook/webhook/abacatepay")
                        .header("X-Webhook-Signature", signature)
                        .content(payload)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(orderRepository).save(order);
    }

    @Test
    void handleWebhook_shouldReturn400WhenInvalidJson() throws Exception {
        String payload = "invalid-json";
        String signature = computeHmacSha256(payload, WEBHOOK_SECRET);

        when(objectMapper.readValue(payload, OrderWebhookController.WebhookPayload.class))
                .thenThrow(new RuntimeException("Invalid JSON"));

        mockMvc.perform(post("/api/orders-webhook/webhook/abacatepay")
                        .header("X-Webhook-Signature", signature)
                        .content(payload)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void handleWebhook_shouldReturn200WhenOrderNotFound() throws Exception {
        UUID orderId = UUID.randomUUID();
        String payload = "{\"event\":\"transparent.completed\",\"data\":{\"transparent\":{\"externalId\":\"" + orderId + "\",\"status\":\"PAID\"}}}";
        String signature = computeHmacSha256(payload, WEBHOOK_SECRET);

        when(objectMapper.readValue(payload, OrderWebhookController.WebhookPayload.class))
                .thenReturn(new OrderWebhookController.WebhookPayload(
                        "transparent.completed",
                        new OrderWebhookController.WebhookData(
                                new OrderWebhookController.WebhookTransparent(orderId.toString(), "PAID")
                        )
                ));
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/orders-webhook/webhook/abacatepay")
                        .header("X-Webhook-Signature", signature)
                        .content(payload)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(orderRepository, never()).save(any());
        verify(messagingTemplate, never()).convertAndSend(anyString(), any());
    }

    private String computeHmacSha256(String data, String secret) {
        try {
            javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
            javax.crypto.spec.SecretKeySpec keySpec = new javax.crypto.spec.SecretKeySpec(
                    secret.getBytes(java.nio.charset.StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(keySpec);
            byte[] hash = mac.doFinal(data.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return java.util.Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
