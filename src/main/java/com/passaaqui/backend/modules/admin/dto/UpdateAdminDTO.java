package com.passaaqui.backend.modules.admin.dto;

import com.passaaqui.backend.modules.admin.model.enums.AdminType;
import com.passaaqui.backend.modules.user.model.enums.UserRole;
import com.passaaqui.backend.shared.validation.password.Password;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UpdateAdminDTO(
        @Email
        @NotBlank
        String email,
        @NotBlank
        String name,
        @Password
        String password,
        @NotBlank
        AdminType adminType,
        @NotBlank
        UserRole role
) {}