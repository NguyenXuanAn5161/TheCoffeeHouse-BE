package com.example.Coffee.controller;

import com.example.Coffee.dto.OrderItemRequest;
import com.example.Coffee.dto.OrderResponse;
import com.example.Coffee.model.enums.OrderStatus;
import com.example.Coffee.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    @Autowired
    private OrderService orderService;

    @PostMapping("/create")
    public ResponseEntity<?> createOrder(@RequestParam Long userId, @RequestBody List<OrderItemRequest> orderItemRequests) {
        try {
            OrderResponse response = orderService.createOrder(userId, orderItemRequests);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Đã xảy ra lỗi khi tạo đơn hàng.");
        }
    }

    // Phương thức GET để lấy danh sách đơn hàng của người dùng
    @GetMapping("/{userId}")
    public List<OrderResponse> getUserOrders(@PathVariable Long userId) {
        try {
            return orderService.getUserOrders(userId);
        } catch (Exception e) {
            throw new RuntimeException("Không thể lấy đơn hàng của người dùng: " + e.getMessage());
        }
    }

    // API để cập nhật trạng thái đơn hàng
    @PutMapping("/{orderId}/status")
    public OrderResponse updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam OrderStatus status) {
        try {
            return orderService.updateOrderStatus(orderId, status);
        } catch (Exception e) {
            throw new RuntimeException("Không thể cập nhật trạng thái đơn hàng: " + e.getMessage());
        }
    }

    // API để hủy đơn hàng
    @PutMapping("/{orderId}/cancel")
    public OrderResponse cancelOrder(@PathVariable Long orderId) {
        try {
            return orderService.cancelOrder(orderId);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Không thể hủy đơn hàng: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Đã xảy ra lỗi khi hủy đơn hàng: " + e.getMessage());
        }
    }
}
