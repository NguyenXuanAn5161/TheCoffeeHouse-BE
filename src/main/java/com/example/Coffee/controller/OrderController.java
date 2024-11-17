package com.example.Coffee.controller;

import com.example.Coffee.dto.ApiResponse;
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

    // API tạo đơn hàng
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @RequestParam Long userId,
            @RequestBody List<OrderItemRequest> orderItemRequests) {
        try {
            orderService.createOrder(userId, orderItemRequests);
            return ResponseEntity.ok(new ApiResponse<>(true, "Đặt hàng thành công!", null));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new ApiResponse<>(false, "Đã xảy ra lỗi khi tạo đơn hàng.", null));
        }
    }

    // API lấy danh sách đơn hàng của người dùng
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getUserOrders(@PathVariable Long userId) {
        try {
            List<OrderResponse> orders = orderService.getUserOrders(userId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Lấy danh sách đơn hàng thành công!", orders));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new ApiResponse<>(false, "Không thể lấy danh sách đơn hàng.", null));
        }
    }

    // API cập nhật trạng thái đơn hàng
    @PutMapping("/{orderId}/status")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam OrderStatus status) {
        try {
            OrderResponse updatedOrder = orderService.updateOrderStatus(orderId, status);
            return ResponseEntity.ok(new ApiResponse<>(true, "Cập nhật trạng thái đơn hàng thành công!", updatedOrder));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new ApiResponse<>(false, "Không thể cập nhật trạng thái đơn hàng.", null));
        }
    }

    // API hủy đơn hàng
    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelOrder(@PathVariable Long orderId) {
        try {
            OrderResponse canceledOrder = orderService.cancelOrder(orderId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Hủy đơn hàng thành công!", canceledOrder));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new ApiResponse<>(false, "Đã xảy ra lỗi khi hủy đơn hàng.", null));
        }
    }
}
