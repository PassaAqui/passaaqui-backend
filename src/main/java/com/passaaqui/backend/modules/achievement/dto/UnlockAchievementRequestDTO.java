package com.passaaqui.backend.modules.achievement.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record UnlockAchievementRequestDTO(
    @JsonProperty("tourist_id")
    Integer touristId
) {}
