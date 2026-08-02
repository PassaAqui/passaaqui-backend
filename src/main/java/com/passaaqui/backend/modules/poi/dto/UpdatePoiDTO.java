package com.passaaqui.backend.modules.poi.dto;

public record UpdatePoiDTO(
    String name,
    String description,
    Integer xpReward,
    Double latitude,
    Double longitude,
    Double minLatitude,
    Double maxLatitude,
    Double minLongitude,
    Double maxLongitude,
    Integer cityId
) {}
