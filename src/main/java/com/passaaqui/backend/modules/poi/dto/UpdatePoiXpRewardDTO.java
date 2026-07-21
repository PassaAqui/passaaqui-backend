package com.passaaqui.backend.modules.poi.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdatePoiXpRewardDTO(
    @NotNull
    @Min(0)
    Integer xpReward
) {}
