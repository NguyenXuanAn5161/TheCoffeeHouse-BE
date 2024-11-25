package com.example.Coffee.service;

import com.example.Coffee.dto.filter.ProductFilterDto;
import com.example.Coffee.model.Product;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

public interface ProductService {
    String createProduct(String name, String description, int quantity, Map<String, Double> sizePrice, String category, MultipartFile image, Boolean isNew, Date newUntil );
    List<Product> getProductFilter(ProductFilterDto filter);
    Product getProductById(Long id);
    String createProductWithLinkImg(String name, String description, int quantity, Map<String, Double> sizePrice, String category, String imageUrl,Boolean isNew, Date newUntil );
    ResponseEntity<String> addProductsFromFile(MultipartFile file) throws IOException;
    ResponseEntity<String> uploadImage(@RequestParam("file") MultipartFile file);

    String deleteProduct(Long id);

}
