package com.passaaqui.backend.modules.order.model.enums;

public enum OrderStatus {
    PENDING,
    AWAITING_PAYMENT,
    PAID,
    PREPARING,
    READY_FOR_PICKUP,
    COMPLETED,
    CANCELED
}