package com.passaaqui.backend.modules.product.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.passaaqui.backend.modules.user.model.UserModel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"product_id", "user_id"}))
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class ProductRatingModel {

    @Id
    @GeneratedValue
    private Integer id;

    @ManyToOne
    @JoinColumn(nullable = false)
    @JsonIgnore
    private ProductModel product;

    @ManyToOne
    @JoinColumn(nullable = false)
    @JsonIgnore
    private UserModel user;

    @Column(nullable = false)
    private Integer rating;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(length = 20)
    private String orderCode;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "product_rating_images", joinColumns = @JoinColumn(name = "rating_id"))
    @Column(name = "image_name")
    private java.util.List<String> images = new java.util.ArrayList<>();

    private String video;

    @Transient
    private java.util.List<String> imageUrls;

    @Transient
    private String videoUrl;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;
}