package com.passaaqui.backend.modules.websocket.service;

import com.passaaqui.backend.modules.websocket.WebSocketTopics;
import com.passaaqui.backend.modules.websocket.dto.PushMessageDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    public <T> void pushToTopic(String topic, String action, T data) {
        messagingTemplate.convertAndSend(topic, new PushMessageDTO<>(action, data));
    }

    public <T> void pushToTopic(String topic, T payload) {
        messagingTemplate.convertAndSend(topic, payload);
    }

    public <T> void pushToUser(String userId, String queue, String action, T data) {
        messagingTemplate.convertAndSendToUser(userId, queue, new PushMessageDTO<>(action, data));
    }

    public <T> void pushToUser(String userId, String queue, T payload) {
        messagingTemplate.convertAndSendToUser(userId, queue, payload);
    }

    public <T> void broadcastOrderStatus(String orderId, String action, T data) {
        pushToTopic(WebSocketTopics.orderStatus(orderId), action, data);
    }
}
