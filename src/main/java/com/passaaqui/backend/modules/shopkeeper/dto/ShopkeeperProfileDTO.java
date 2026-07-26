package com.passaaqui.backend.modules.shopkeeper.dto;

import com.passaaqui.backend.modules.category.model.CategoryModel;
import com.passaaqui.backend.modules.poi.model.PoiModel;
import com.passaaqui.backend.modules.shopkeeper.model.ShopkeeperModel;
import java.time.LocalDateTime;

public record ShopkeeperProfileDTO(
    Integer id,
    String name,
    String email,
    String documentId,
    String companyName,
    String description,
    String image,
    CategoryModel category,
    PoiModel poi,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static ShopkeeperProfileDTO from(ShopkeeperModel shopkeeper, PoiModel poi) {
        return new ShopkeeperProfileDTO(
            shopkeeper.getId(),
            shopkeeper.getName(),
            shopkeeper.getEmail(),
            shopkeeper.getDocumentId(),
            shopkeeper.getCompanyName(),
            shopkeeper.getDescription(),
            shopkeeper.getImage(),
            shopkeeper.getCategory(),
            poi,
            shopkeeper.getCreatedAt(),
            shopkeeper.getUpdatedAt()
        );
    }
}
