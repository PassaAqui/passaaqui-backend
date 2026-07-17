package com.passaaqui.backend.modules.product.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.passaaqui.backend.modules.product.model.ProductModel;

public record ProductDTO(
    Integer id,
    String name,
    String description,
    Double price,
    @JsonProperty("max_xp") Integer maxXp,
    Integer stock,
    String image,
    @JsonProperty("shopkeeper_id") Integer shopkeeperId,
    @JsonProperty("category_id") Integer categoryId
) {
    public static ProductDTO from(ProductModel product, String imageUrl) {
        return new ProductDTO(
            product.getId(), product.getName(), product.getDescription(),
            product.getPrice(), product.getMaxXp(), product.getStock(),
            imageUrl,
            product.getShopkeeper().getId(), product.getCategory().getId()
        );
    }
}
