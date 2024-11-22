package com.example.Coffee.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartItemDto {
    private Long id;
    private String imageUrl;
    private Long productId;
    private String productName;
    private String size;
    private int quantity;
    private double price;
    private Date updatedAt;
}
