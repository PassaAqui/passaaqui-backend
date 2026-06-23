package com.passaaqui.backend.unit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.passaaqui.backend.infra.exception.GlobalExceptionHandler;
import com.passaaqui.backend.infra.exception.ResourceNotFoundException;
import com.passaaqui.backend.infra.exception.ConflictException;
import com.passaaqui.backend.modules.order.controller.OrderController;
import com.passaaqui.backend.modules.order.dto.CheckoutRequestDTO;
import com.passaaqui.backend.modules.order.dto.OrderResponseDTO;
import com.passaaqui.backend.modules.order.model.enums.OrderStatus;
import com.passaaqui.backend.modules.order.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    private MockMvc mockMvc;

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderController controller;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void checkout_shouldReturn200() throws Exception {
        CheckoutRequestDTO dto = new CheckoutRequestDTO(1);
        UUID orderId = UUID.randomUUID();
        OrderResponseDTO response = new OrderResponseDTO(
                orderId, 1, "Product", 2, "Shop", 1,
                BigDecimal.TEN, BigDecimal.TEN, OrderStatus.AWAITING_PAYMENT,
                "tx-123", LocalDateTime.now(), "pix-code", "qr-base64",
                LocalDateTime.now().plusMinutes(10), null
        );

        when(orderService.checkout(any(CheckoutRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/orders/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId.toString()))
                .andExpect(jsonPath("$.status").value("AWAITING_PAYMENT"));
    }

    @Test
    void checkout_shouldReturn400WhenInvalid() throws Exception {
        CheckoutRequestDTO dto = new CheckoutRequestDTO(null);

        mockMvc.perform(post("/api/orders/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void checkout_shouldReturn409WhenActiveOrderExists() throws Exception {
        CheckoutRequestDTO dto = new CheckoutRequestDTO(1);
        when(orderService.checkout(any(CheckoutRequestDTO.class)))
                .thenThrow(new ConflictException("Você já possui um pedido ativo."));

        mockMvc.perform(post("/api/orders/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict());
    }

    @Test
    void checkout_shouldReturn404WhenProductNotFound() throws Exception {
        CheckoutRequestDTO dto = new CheckoutRequestDTO(999);
        when(orderService.checkout(any(CheckoutRequestDTO.class)))
                .thenThrow(new ResourceNotFoundException("Product not found: 999"));

        mockMvc.perform(post("/api/orders/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void getShopkeeperOrders_shouldReturn200() throws Exception {
        UUID orderId = UUID.randomUUID();
        OrderResponseDTO response = new OrderResponseDTO(
                orderId, 1, "Product", 2, "Shop", 1,
                BigDecimal.TEN, BigDecimal.TEN, OrderStatus.PAID,
                "tx-123", LocalDateTime.now(), null, null, null, "CODE123"
        );

        when(orderService.getShopkeeperOrders()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/orders/shopkeeper"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(orderId.toString()))
                .andExpect(jsonPath("$[0].pickupCode").value("CODE123"));
    }

    @Test
    void getShopkeeperOrders_shouldReturn200EmptyList() throws Exception {
        when(orderService.getShopkeeperOrders()).thenReturn(List.of());

        mockMvc.perform(get("/api/orders/shopkeeper"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getMyCurrentOrder_shouldReturn200() throws Exception {
        UUID orderId = UUID.randomUUID();
        OrderResponseDTO response = new OrderResponseDTO(
                orderId, 1, "Product", 2, "Shop", 1,
                BigDecimal.TEN, BigDecimal.TEN, OrderStatus.PAID,
                "tx-123", LocalDateTime.now(), null, null, null, "CODE456"
        );

        when(orderService.getMyCurrentOrder()).thenReturn(response);

        mockMvc.perform(get("/api/orders/my-current"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId.toString()))
                .andExpect(jsonPath("$.pickupCode").value("CODE456"));
    }

    @Test
    void getMyCurrentOrder_shouldReturn404WhenNoPaidOrder() throws Exception {
        when(orderService.getMyCurrentOrder())
                .thenThrow(new ResourceNotFoundException("No paid order found"));

        mockMvc.perform(get("/api/orders/my-current"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getShopkeeperHistory_shouldReturn200() throws Exception {
        UUID orderId = UUID.randomUUID();
        OrderResponseDTO response = new OrderResponseDTO(
                orderId, 1, "Product", 2, "Shop", 1,
                BigDecimal.TEN, BigDecimal.TEN, OrderStatus.PAID,
                "tx-123", LocalDateTime.now(), null, null, null, "CODE123"
        );

        when(orderService.getShopkeeperHistory()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/orders/shopkeeper/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(orderId.toString()))
                .andExpect(jsonPath("$[0].pickupCode").value("CODE123"));
    }

    @Test
    void getTouristHistory_shouldReturn200() throws Exception {
        UUID orderId = UUID.randomUUID();
        OrderResponseDTO response = new OrderResponseDTO(
                orderId, 1, "Product", 2, "Shop", 1,
                BigDecimal.TEN, BigDecimal.TEN, OrderStatus.AWAITING_PAYMENT,
                "tx-123", LocalDateTime.now(), "pix-code", "qr-base64",
                LocalDateTime.now().plusMinutes(10), null
        );

        when(orderService.getTouristHistory()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/orders/my-history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(orderId.toString()))
                .andExpect(jsonPath("$[0].status").value("AWAITING_PAYMENT"));
    }
}
