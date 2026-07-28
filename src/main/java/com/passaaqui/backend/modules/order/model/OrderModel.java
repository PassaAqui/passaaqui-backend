package com.passaaqui.backend.modules.order.model;

import com.passaaqui.backend.modules.order.model.enums.OrderStatus;
import com.passaaqui.backend.modules.tourist.model.TouristModel;
import com.passaaqui.backend.modules.shopkeeper.model.ShopkeeperModel;
import com.passaaqui.backend.modules.product.model.ProductModel;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tb_orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderModel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tourist_id", nullable = false)
    private TouristModel tourist;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shopkeeper_id", nullable = false)
    private ShopkeeperModel shopkeeper;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private ProductModel product;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private BigDecimal totalAmount;

    @Builder.Default
    @Column(nullable = false)
    private BigDecimal cashDiscount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(length = 10)
    private String code;

    @Column(length = 6)
    private String redemptionCode;

    @Column(length = 6)
    private String pickupCode;

    @Column(unique = true)
    private String transactionId;

    @Column(columnDefinition = "TEXT")
    private String qrCodeUrl;

    @Column(length = 1000)
    private String pix;

    private LocalDateTime pixExpiresAt;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}