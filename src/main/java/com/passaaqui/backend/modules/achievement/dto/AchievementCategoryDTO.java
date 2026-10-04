package com.passaaqui.backend.modules.achievement.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AchievementCategoryDTO(
    @JsonProperty("value")
    String value,

    @JsonProperty("label")
    String label,

    @JsonProperty("description")
    String description
) {}
