package com.example.Coffee.service;

import com.example.Coffee.model.Product;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface ProductService {
    String createProduct(String name, String description, int quantity, Map<String, Double> sizePrice, String category, MultipartFile image);
    List<Product> getAllProducts();
    Product getProductById(Long id);
    String deleteProduct(Long id);
}
