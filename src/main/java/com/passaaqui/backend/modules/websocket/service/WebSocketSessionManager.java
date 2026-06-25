package com.passaaqui.backend.modules.websocket.service;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class WebSocketSessionManager {

    private final Map<String, List<String>> userSessions = new ConcurrentHashMap<>();

    @EventListener
    public void handleSessionConnected(SessionConnectedEvent event) {
        Principal principal = event.getUser();
        if (principal != null) {
            userSessions
                    .computeIfAbsent(principal.getName(), k -> new CopyOnWriteArrayList<>())
                    .add(event.getMessage().getHeaders().get("simpSessionId", String.class));
        }
    }

    @EventListener
    public void handleSessionDisconnected(SessionDisconnectEvent event) {
        Principal principal = event.getUser();
        if (principal != null) {
            List<String> sessions = userSessions.get(principal.getName());
            if (sessions != null) {
                sessions.remove(event.getSessionId());
                if (sessions.isEmpty()) {
                    userSessions.remove(principal.getName());
                }
            }
        }
    }

    public List<String> getSessionIds(String userId) {
        return userSessions.getOrDefault(userId, List.of());
    }

    public boolean isConnected(String userId) {
        return userSessions.containsKey(userId) && !userSessions.get(userId).isEmpty();
    }

    public void removeUser(String userId) {
        userSessions.remove(userId);
    }
}
