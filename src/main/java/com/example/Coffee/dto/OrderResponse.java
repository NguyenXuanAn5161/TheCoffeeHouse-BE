package com.example.Coffee.dto;

import com.example.Coffee.model.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private Long orderId;
    private double totalPrice;
    private OrderStatus status;
    private List<OrderItemResponse> items;
}
