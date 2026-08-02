package com.passaaqui.backend.infra.integration.abacatepay.dto;

public record AbacateBaseResponseDTO<T>(
        boolean success,
        T data,
        String error
) {}