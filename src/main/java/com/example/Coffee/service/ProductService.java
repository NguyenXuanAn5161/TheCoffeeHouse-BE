package com.example.Coffee.service;

import com.example.Coffee.model.Product;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface ProductService {
    String createProduct(String name, String description, int quantity, Map<String, Double> sizePrice, String category, MultipartFile image);
    List<Product> getAllProducts();
    Product getProductById(Long id);
    String createProductWithLinkImg(String name, String description, int quantity, Map<String, Double> sizePrice, String category, String imageUrl);
    ResponseEntity<String> addProductsFromFile(MultipartFile file) throws IOException;
    ResponseEntity<String> uploadImage(@RequestParam("file") MultipartFile file);

    String deleteProduct(Long id);

}
