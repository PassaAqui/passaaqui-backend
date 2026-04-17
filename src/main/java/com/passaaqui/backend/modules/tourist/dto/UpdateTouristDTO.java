package com.passaaqui.backend.modules.tourist.dto;

import com.passaaqui.backend.shared.validation.password.Password;
import jakarta.validation.constraints.NotBlank;

public record UpdateTouristDTO(
    @NotBlank
    String name,
    @Password
    String password,
    @NotBlank
    String documentId
) {}