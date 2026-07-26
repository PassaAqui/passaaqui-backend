package com.passaaqui.backend.modules.product.dto;

import com.passaaqui.backend.modules.product.model.ProductModel;

public record ShopkeeperProductDTO(
    Integer id,
    String name,
    Double price,
    String image,
    Boolean active,
    Boolean highlight,
    String category
) {
    public static ShopkeeperProductDTO from(ProductModel product) {
        String image = product.getImageUrls() != null && !product.getImageUrls().isEmpty()
            ? product.getImageUrls().get(0)
            : null;
        return new ShopkeeperProductDTO(
            product.getId(),
            product.getName(),
            product.getPrice(),
            image,
            product.getActive(),
            product.getHighlight(),
            product.getCategory().getName()
        );
    }
}