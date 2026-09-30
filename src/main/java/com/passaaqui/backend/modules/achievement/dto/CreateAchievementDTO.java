package com.passaaqui.backend.modules.achievement.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateAchievementDTO(
    @NotBlank(message = "Achievement name is required")
    @Size(max = 150, message = "Achievement name must not exceed 150 characters")
    String name,

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    String description,

    @JsonProperty("xp_reward")
    Integer xpReward,

    @JsonProperty("category_id")
    Integer categoryId,

    @JsonProperty("category")
    String category,

    @Size(max = 200, message = "Location must not exceed 200 characters")
    String location,

    @JsonProperty("poi_id")
    Integer poiId
) {
    public CreateAchievementDTO(String name, String description, Integer xpReward, Integer categoryId, String location, Integer poiId) {
        this(name, description, xpReward, categoryId, null, location, poiId);
    }
}
