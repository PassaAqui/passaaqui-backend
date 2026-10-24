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
    List<ProductDTO> products,
    @JsonProperty("estimated_xp") Integer estimatedXp
) {
    public PoiDetailDTO(
        Integer id,
        String name,
        String description,
        Integer xpReward,
        PoiType type,
        Double latitude,
        Double longitude,
        Double averageRating,
        Integer ratingsCount,
        String image,
        List<ProductDTO> products
    ) {
        this(id, name, description, xpReward, type, latitude, longitude, averageRating, ratingsCount, image, products, null);
    }

    public static PoiDetailDTO from(PoiModel poi, String imageUrl, List<ProductDTO> products) {
        return from(poi, imageUrl, products, null);
    }

    public static PoiDetailDTO from(PoiModel poi, String imageUrl, List<ProductDTO> products, Integer estimatedXp) {
        return new PoiDetailDTO(
            poi.getId(), poi.getName(), poi.getDescription(),
            poi.getXpReward(), poi.getType(),
            poi.getLatitude(), poi.getLongitude(),
            poi.getAverageRating(), poi.getRatingsCount(),
            imageUrl, products, estimatedXp
        );
    }
}
