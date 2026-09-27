package com.passaaqui.backend.modules.order.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum RedemptionStatus {
    NAO_RESGATADO("nao_resgatado"),
    RESGATADO("resgatado");

    private final String value;

    RedemptionStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
