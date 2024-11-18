package com.example.Coffee.webSocket;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class WebSocketHandlerImpl implements WebSocketHandler {
    private final Set<WebSocketSession> sessions = Collections.newSetFromMap(new ConcurrentHashMap<>());

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
        System.out.println("Kết nối mới: " + session.getId());
        session.sendMessage(new TextMessage("Connected"));
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        System.out.println("Nhận tin nhắn từ client: " + message.getPayload());
    }

    public void sendOrderStatusUpdate(String orderStatus) {
        for (WebSocketSession session : sessions) {
            try {
                session.sendMessage(new TextMessage(orderStatus));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        System.out.println("Lỗi xảy ra: " + exception.getMessage());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        sessions.remove(session);
        System.out.println("Kết nối đóng: " + session.getId());
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
}
