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
    Integer categoryId
) {}
