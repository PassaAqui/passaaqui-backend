package com.passaaqui.backend.modules.achievement.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public record AchievementResponseDTO(
    @JsonProperty("achievement_id")
    Integer achievementId,

    @JsonProperty("name")
    String name,

    @JsonProperty("description")
    String description,

    @JsonProperty("photo_url")
    String photoUrl,

    @JsonProperty("xp_reward")
    Integer xpReward,

    @JsonProperty("category_id")
    Integer categoryId,

    @JsonProperty("category_name")
    String categoryName,

    @JsonProperty("location")
    String location,

    @JsonProperty("poi_id")
    Integer poiId,

    @JsonProperty("poi_name")
    String poiName,

    @JsonProperty("unlocked")
    boolean unlocked,

    @JsonProperty("unlocked_at")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime unlockedAt
) {}
