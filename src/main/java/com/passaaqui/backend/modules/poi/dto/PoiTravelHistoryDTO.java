package com.passaaqui.backend.modules.poi.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.passaaqui.backend.modules.poi.model.enums.PoiType;

import java.time.LocalDateTime;

public record PoiTravelHistoryDTO(
    @JsonProperty("visit_id")
    Integer visitId,

    @JsonProperty("poi_id")
    Integer poiId,

    @JsonProperty("poi_name")
    String poiName,

    @JsonProperty("poi_description")
    String poiDescription,

    @JsonProperty("image_url")
    String imageUrl,

    @JsonProperty("poi_type")
    PoiType poiType,

    @JsonProperty("city_name")
    String cityName,

    @JsonProperty("xp_earned")
    Integer xpEarned,

    @JsonProperty("distance_km")
    Double distanceKm,

    @JsonProperty("visited_at")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime visitedAt
) {}
