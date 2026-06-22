package com.passaaqui.backend.modules.admin.dto;

import com.passaaqui.backend.modules.admin.model.enums.AdminType;
import com.passaaqui.backend.modules.admin.model.enums.AdminType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateAdminDTO(
        @Email
        @NotBlank
        String email,
        @NotBlank
        String name,
        String password,
        @NotNull
        AdminType adminType
) {}