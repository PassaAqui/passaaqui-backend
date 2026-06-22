package com.passaaqui.backend.modules.category.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateCategoryDTO(
    @NotBlank
    String name,

    String description,

    Double categoryWeight
) {}
