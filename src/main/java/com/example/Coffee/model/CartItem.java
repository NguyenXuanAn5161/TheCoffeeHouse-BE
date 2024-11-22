package com.example.Coffee.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    private String size;
    private int quantity;
    private double price; // Giá tại thời điểm thêm vào giỏ hàng

    @ManyToOne
    @JoinColumn(name = "cart_id")
    private ShoppingCart cart;

    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;
}
