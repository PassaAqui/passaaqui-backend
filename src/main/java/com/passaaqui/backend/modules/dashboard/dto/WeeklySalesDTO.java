package com.passaaqui.backend.modules.dashboard.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

public record WeeklySalesDTO(
    String day,
    BigDecimal total
) {}