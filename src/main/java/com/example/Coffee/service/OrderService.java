package com.example.Coffee.service;

import com.example.Coffee.dto.OrderItemRequest;
import com.example.Coffee.dto.OrderResponse;
import com.example.Coffee.model.enums.OrderStatus;

import java.util.List;

public interface OrderService {
    OrderResponse createOrder(Long userId, List<OrderItemRequest> orderItemRequests);
    OrderResponse updateOrderStatus(Long orderId, OrderStatus orderStatus, Long userId);
    OrderResponse cancelOrder(Long orderId);
    List<OrderResponse> getUserOrders(Long userId);  // Phương thức để lấy danh sách đơn hàng của người dùng
}
