package com.passaaqui.backend.infra.abacatepay.dto;

public record AbacateBaseResponseDTO<T>(
        boolean success,
        T data,
        String error
) {}