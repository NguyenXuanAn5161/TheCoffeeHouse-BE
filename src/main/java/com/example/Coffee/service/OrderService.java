package com.example.Coffee.service;

import com.example.Coffee.dto.OrderItemRequest;
import com.example.Coffee.dto.OrderResponse;
import java.util.List;

public interface OrderService {
    OrderResponse createOrder(Long userId, List<OrderItemRequest> orderItemRequests);
    List<OrderResponse> getUserOrders(Long userId);  // Phương thức để lấy danh sách đơn hàng của người dùng
}
