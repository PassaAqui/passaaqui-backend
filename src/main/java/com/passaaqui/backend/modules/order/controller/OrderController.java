package com.passaaqui.backend.modules.order.controller;

import com.passaaqui.backend.modules.order.dto.CheckoutRequestDTO;
import com.passaaqui.backend.modules.order.dto.OrderResponseDTO;
import com.passaaqui.backend.modules.order.dto.ShopkeeperOrderDTO;
import com.passaaqui.backend.modules.order.dto.UpdateOrderStatusDTO;
import com.passaaqui.backend.modules.order.model.enums.OrderStatus;
import com.passaaqui.backend.modules.order.service.OrderService;
import java.util.List;
import java.util.UUID;
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
    public ResponseEntity<List<ShopkeeperOrderDTO>> getShopkeeperOrders(
            @RequestParam(required = false) OrderStatus status) {
        return ResponseEntity.ok(orderService.getShopkeeperOrdersByStatus(status));
    }

    @GetMapping("/shopkeeper/history")
    @PreAuthorize("hasRole('SHOPKEEPER')")
    public ResponseEntity<List<OrderResponseDTO>> getShopkeeperHistory() {
        return ResponseEntity.ok(orderService.getShopkeeperHistory());
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('SHOPKEEPER')")
    public ResponseEntity<ShopkeeperOrderDTO> updateOrderStatus(
            @PathVariable UUID id,
            @RequestBody @Valid UpdateOrderStatusDTO dto) {
        return ResponseEntity.ok(orderService.updateOrderStatus(id, dto));
    }

    @GetMapping("/my-history")
    @PreAuthorize("hasRole('TOURIST')")
    public ResponseEntity<List<OrderResponseDTO>> getTouristHistory() {
        return ResponseEntity.ok(orderService.getTouristHistory());
    }

    @GetMapping("/my-current")
    @PreAuthorize("hasRole('TOURIST')")
    public ResponseEntity<OrderResponseDTO> getMyCurrentOrder() {
        return orderService.getMyCurrentOrder()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('TOURIST', 'SHOPKEEPER', 'ADMIN_USER', 'ADMIN_ROOT')")
    public ResponseEntity<OrderResponseDTO> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(orderService.findById(id));
    }
}
