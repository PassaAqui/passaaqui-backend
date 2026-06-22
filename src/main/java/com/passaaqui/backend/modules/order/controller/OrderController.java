package com.passaaqui.backend.modules.order.controller;

import com.passaaqui.backend.modules.order.dto.CheckoutRequestDTO;
import com.passaaqui.backend.modules.order.dto.OrderResponseDTO;
import com.passaaqui.backend.modules.order.service.OrderService;
import java.util.List;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/checkout")
    @PreAuthorize("hasRole('TOURIST')")
    public ResponseEntity<OrderResponseDTO> checkout(@RequestBody @Valid CheckoutRequestDTO request) {
        return ResponseEntity.ok(orderService.checkout(request));
    }

    @GetMapping("/shopkeeper")
    @PreAuthorize("hasRole('SHOPKEEPER')")
    public ResponseEntity<List<OrderResponseDTO>> getShopkeeperOrders() {
        return ResponseEntity.ok(orderService.getShopkeeperOrders());
    }

    @GetMapping("/my-current")
    @PreAuthorize("hasRole('TOURIST')")
    public ResponseEntity<OrderResponseDTO> getMyCurrentOrder() {
        return ResponseEntity.ok(orderService.getMyCurrentOrder());
    }
}
