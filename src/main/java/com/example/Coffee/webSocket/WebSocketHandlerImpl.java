package com.example.Coffee.webSocket;

import com.example.Coffee.dto.OrderResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WebSocketHandlerImpl implements WebSocketHandler {
    private final Set<WebSocketSession> sessions = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final Map<String, WebSocketSession> userSessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // Lấy userId từ query parameter
        String userId = UriComponentsBuilder.fromUri(session.getUri()).build().getQueryParams().getFirst("userId");
        System.out.println("Kết nối mới với userId (từ query parameter): " + userId);

        if (userId != null && !userId.isEmpty()) {
            userSessions.put(userId, session);
            System.out.println("Session được lưu cho userId: " + userId);
            System.out.println("Danh sách userSessions hiện tại khi vừa kết nối thành công: " + userSessions.keySet());
            session.sendMessage(new TextMessage("Connected"));
        } else {
            System.out.println("Không nhận được userId từ query parameter.");
        }

        sessions.add(session);
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        System.out.println("Nhận tin nhắn từ client: " + message.getPayload());
    }

    // Gửi thông báo trạng thái đơn hàng đến đúng người dùng
    public void sendOrderStatusUpdate(String userId, OrderResponse orderResponse) {
        System.out.println("Danh sách userSessions hiện tại: " + userSessions.keySet());

        WebSocketSession session = userSessions.get(userId);
        System.out.println("Gửi thông báo tới userId: " + userId);
        if (session != null) {
            System.out.println("Session tồn tại cho userId: " + userId);
            if (session.isOpen()) {
                try {
                    // Chuyển đổi orderResponse thành chuỗi JSON
                    ObjectMapper objectMapper = new ObjectMapper();
                    String jsonMessage = objectMapper.writeValueAsString(orderResponse);
                    session.sendMessage(new TextMessage(jsonMessage));
                    System.out.println("Thông báo đã được gửi: " + jsonMessage);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("Session đã đóng cho userId: " + userId);
            }
        } else {
            System.out.println("Không tìm thấy session cho userId: " + userId);
        }
    }


    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        System.out.println("Lỗi xảy ra: " + exception.getMessage());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        // Xóa userId khỏi map khi kết nối đóng
        userSessions.values().remove(session);
        sessions.remove(session);
        System.out.println("Kết nối đóng: " + session.getId());
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
}
