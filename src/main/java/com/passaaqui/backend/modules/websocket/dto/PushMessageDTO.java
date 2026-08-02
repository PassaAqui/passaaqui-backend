package com.passaaqui.backend.modules.websocket.dto;

public record PushMessageDTO<T>(
        String action,
        T data
) {
}
