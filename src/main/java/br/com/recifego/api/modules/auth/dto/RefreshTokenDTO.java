package br.com.recifego.api.modules.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenDTO(
    @NotBlank String refreshToken
) {}