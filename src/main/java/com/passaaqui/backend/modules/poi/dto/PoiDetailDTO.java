package com.passaaqui.backend.modules.poi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.passaaqui.backend.modules.poi.model.PoiModel;
import com.passaaqui.backend.modules.poi.model.enums.PoiType;
import com.passaaqui.backend.modules.product.dto.ProductDTO;

import java.util.List;

public record PoiDetailDTO(
    Integer id,
    String name,
    String description,
    @JsonProperty("xp_reward") Integer xpReward,
    PoiType type,
    Double latitude,
    Double longitude,
    @JsonProperty("average_rating") Double averageRating,
    @JsonProperty("ratings_count") Integer ratingsCount,
    String image,
    List<ProductDTO> products
) {
    public static PoiDetailDTO from(PoiModel poi, String imageUrl, List<ProductDTO> products) {
        return new PoiDetailDTO(
            poi.getId(), poi.getName(), poi.getDescription(),
            poi.getXpReward(), poi.getType(),
            poi.getLatitude(), poi.getLongitude(),
            poi.getAverageRating(), poi.getRatingsCount(),
            imageUrl, products
        );
    }
}
