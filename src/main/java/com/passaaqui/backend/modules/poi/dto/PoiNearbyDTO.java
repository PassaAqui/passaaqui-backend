package com.passaaqui.backend.modules.poi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.passaaqui.backend.modules.poi.model.PoiModel;
import com.passaaqui.backend.modules.poi.model.enums.PoiType;

public record PoiNearbyDTO(
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
    @JsonProperty("distance_km") Double distanceKm
) {
    public static PoiNearbyDTO from(PoiModel poi, Double distanceKm, String imageUrl) {
        return new PoiNearbyDTO(
            poi.getId(), poi.getName(), poi.getDescription(),
            poi.getXpReward(), poi.getType(),
            poi.getLatitude(), poi.getLongitude(),
            poi.getAverageRating(), poi.getRatingsCount(),
            imageUrl, distanceKm
        );
    }
}
