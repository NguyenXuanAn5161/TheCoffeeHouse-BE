package com.example.Coffee.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.Map;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    private int quantity;
    private String category; // "Coffee", "Chocolate", "Food"
    private String imageUrl; // URL của hình ảnh

    // Gộp size và giá thành một field
    @ElementCollection
    @CollectionTable(name = "product_size_price", joinColumns = @JoinColumn(name = "product_id"))
    @MapKeyColumn(name = "size")
    @Column(name = "price")
    private Map<String, Double> sizePrice; // Ví dụ: {"S": 30.0, "M": 35.0, "L": 40.0}

    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;
}
