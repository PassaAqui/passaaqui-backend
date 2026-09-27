package com.passaaqui.backend.modules.user.dto;

import com.passaaqui.backend.modules.user.model.enums.ThemePreference;
import jakarta.validation.constraints.NotNull;

public record UpdateThemeDTO(
    @NotNull(message = "Theme is required")
    ThemePreference theme
) {}
