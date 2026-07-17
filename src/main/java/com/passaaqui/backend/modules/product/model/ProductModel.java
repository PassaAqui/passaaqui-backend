package com.passaaqui.backend.modules.product.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.passaaqui.backend.modules.category.model.CategoryModel;
import com.passaaqui.backend.modules.poi.model.PoiModel;
import com.passaaqui.backend.modules.shopkeeper.model.ShopkeeperModel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class ProductModel {

    @Id
    @GeneratedValue
    private Integer id;

    @Column(nullable = false)
    private String name;

    private String description;

    private Double price;

    private Integer maxXp;

    @Column(nullable = false)
    private Integer stock = 0;

    private String image;

    @Transient
    @JsonProperty("image")
    private String imageUrl;

    private Double averageRating;

    private Integer ratingsCount;

    @JsonIgnore
    public String getImage() {
        return image;
    }

    @Version
    private Integer version;

    @ManyToOne
    @JoinColumn(nullable = false)
    private ShopkeeperModel shopkeeper;

    @ManyToOne
    @JoinColumn(nullable = false)
    private CategoryModel category;

    @ManyToOne
    @JoinColumn(nullable = false)
    private PoiModel poi;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
