package com.example.Coffee.service.serviceImpl;

import com.example.Coffee.model.Product;
import com.example.Coffee.repository.ProductRepository;
import com.example.Coffee.service.ProductService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@Service
    public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/";
    @Autowired
    private ObjectMapper jacksonObjectMapper;

    @Override
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    public Product getProductById(Long id) {
        Optional<Product> product = productRepository.findById(id);
        return product.orElse(null);  // Nếu không tìm thấy, trả về null
    }

    // Method to handle creating product and storing image
    @Override
    public String createProduct(String name, String description, int quantity,
                                Map<String, Double> sizePrice, String category, MultipartFile image) {
        try {
            // Upload hình ảnh lên Imgur
            ResponseEntity<String> imageUrlResponse = uploadImage(image);
            String link = getImageUrl(imageUrlResponse);

            // Tạo đối tượng Product
            Product product = new Product();
            product.setName(name);
            product.setDescription(description);
            product.setQuantity(quantity);
            product.setSizePrice(sizePrice); // Đặt thông tin size và giá
            product.setCategory(category);
            product.setImageUrl(link);
            product.setCreatedAt(new Date());
            product.setUpdatedAt(new Date());

            // Lưu sản phẩm vào cơ sở dữ liệu
            productRepository.save(product);
            return "Sản phẩm đã được tạo thành công";
        } catch (Exception e) {
            return "Có lỗi xảy ra khi tạo sản phẩm: " + e.getMessage();
        }
    }


    private static final String IMGUR_API_URL = "https://api.imgur.com/3/upload";
    private static final String CLIENT_ID = "YOUR_IMGUR_CLIENT_ID"; // Thay bằng Client ID của bạn

    public ResponseEntity<String> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            // Chuyển file thành byte array
            byte[] fileBytes = file.getBytes();

            // Tạo header
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Client-ID " + CLIENT_ID);

            // Tạo body của request
            Map<String, Object> body = new HashMap<>();
            body.put("image", fileBytes);
            body.put("type", "base64");

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            // Gửi yêu cầu POST đến API Imgur
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.exchange(IMGUR_API_URL, HttpMethod.POST, entity, String.class);

            // Xử lý phản hồi để lấy URL ảnh
            if (response.getStatusCode() == HttpStatus.OK) {
                String responseBody = response.getBody();
                // Bạn cần phân tích phản hồi JSON để lấy URL ảnh (dùng Jackson hoặc Gson)
                String imageUrl = extractImageUrlFromResponse(responseBody);
                return ResponseEntity.ok(imageUrl);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Tải ảnh lên thất bại.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi tải ảnh lên: " + e.getMessage());
        }
    }

    public String getImageUrl(ResponseEntity<String> imageUrlResponse) throws IOException {
        // Phân tích JSON thành JsonNode
        String responseBody = imageUrlResponse.getBody();
        JsonNode rootNode = jacksonObjectMapper.readTree(responseBody);

        // Lấy URL từ trường 'link'
        JsonNode dataNode = rootNode.path("data");
        if (dataNode != null && dataNode.has("link")) {
            return dataNode.get("link").asText(); // Trả về URL ảnh
        } else {
            throw new IOException("Không tìm thấy link trong phản hồi Imgur.");
        }
    }

    private String extractImageUrlFromResponse(String responseBody) {
        // Phân tích JSON ở đây để lấy URL ảnh từ phản hồi của Imgur
        // Đơn giản, bạn có thể sử dụng thư viện Jackson hoặc Gson để phân tích JSON
        return responseBody; // Đây chỉ là ví dụ, bạn cần implement phân tích JSON thực tế
    }

    @Override
    public String deleteProduct(Long id) {
        if (productRepository.existsById(id)) {
            productRepository.deleteById(id);
            return "Sản phẩm đã được xóa thành công";
        }
        return "Không tìm thấy sản phẩm với ID: " + id;
    }
}
