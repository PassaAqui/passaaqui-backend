package com.passaaqui.backend.modules.product.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.passaaqui.backend.modules.product.model.ProductModel;

import java.util.List;

public record ProductDTO(
    Integer id,
    String name,
    String description,
    Double price,
    @JsonProperty("max_xp") Integer maxXp,
    Integer stock,
    @JsonProperty("images") List<String> images,
    @JsonProperty("average_rating") Double averageRating,
    @JsonProperty("ratings_count") Integer ratingsCount,
    @JsonProperty("shopkeeper_id") Integer shopkeeperId,
    @JsonProperty("category_id") Integer categoryId
) {
    public static ProductDTO from(ProductModel product, List<String> imageUrls) {
        return new ProductDTO(
            product.getId(), product.getName(), product.getDescription(),
            product.getPrice(), product.getMaxXp(), product.getStock(),
            imageUrls,
            product.getAverageRating(), product.getRatingsCount(),
            product.getShopkeeper().getId(), product.getCategory().getId()
        );
    }
}
