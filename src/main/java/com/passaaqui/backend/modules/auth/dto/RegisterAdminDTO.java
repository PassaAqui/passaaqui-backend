package com.passaaqui.backend.modules.auth.dto;

import com.passaaqui.backend.modules.admin.model.enums.AdminType;
import com.passaaqui.backend.shared.validation.password.Password;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RegisterAdminDTO(
        @Email
        @NotBlank
        String email,
        @NotBlank
        String name,

        @Password
        String password,

        @Password
        String confirm_password,
        @NotNull
        AdminType adminType
) {}
