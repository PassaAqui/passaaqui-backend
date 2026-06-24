package com.passaaqui.backend.modules.websocket;

public final class WebSocketTopics {

    public static final String PREFIX = "/topic";
    public static final String QUEUE_PREFIX = "/queue";

    public static final String ORDERS = PREFIX + "/orders";
    public static final String ROUTES = PREFIX + "/routes";

    private WebSocketTopics() {
    }

    public static String orderStatus(String orderId) {
        return ORDERS + "/" + orderId;
    }

    public static String routeSession(String userId) {
        return ROUTES + "/" + userId;
    }

    public static String userQueue(String userId) {
        return QUEUE_PREFIX + "/" + userId;
    }
}
