package com.passaaqui.backend.modules.category.dto;

import com.passaaqui.backend.modules.category.model.CategoryModel;
import com.passaaqui.backend.modules.product.model.ProductModel;
import org.springframework.data.domain.Page;

public record CategoryFeedDTO(
    Integer id,
    String name,
    String description,
    Page<ProductModel> products
) {
    public static CategoryFeedDTO from(CategoryModel category, Page<ProductModel> products) {
        return new CategoryFeedDTO(
            category.getId(),
            category.getName(),
            category.getDescription(),
            products
        );
    }
}