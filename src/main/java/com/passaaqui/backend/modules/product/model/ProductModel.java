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
import java.util.ArrayList;
import java.util.List;

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

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "product_images", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "image_name")
    private List<String> images = new ArrayList<>();

    @Transient
    @JsonProperty("images")
    private List<String> imageUrls;

    private Double averageRating;

    private Integer ratingsCount;

    @JsonIgnore
    public List<String> getImages() {
        return images;
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

    @Column(nullable = false)
    private Boolean active = true;

    @Column(nullable = false)
    private Boolean highlight = false;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
