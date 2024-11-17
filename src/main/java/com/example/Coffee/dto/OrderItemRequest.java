package com.example.Coffee.dto;

import com.example.Coffee.model.enums.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemRequest {
    private Long productId;
    private String size;
    private int quantity;
    private PaymentMethod paymentMethod;
}
