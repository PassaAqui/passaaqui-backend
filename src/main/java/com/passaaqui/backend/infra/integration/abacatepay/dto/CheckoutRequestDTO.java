package com.passaaqui.backend.infra.integration.abacatepay.dto;

import java.util.UUID;

public record CheckoutRequestDTO(double price, UUID id) {}
