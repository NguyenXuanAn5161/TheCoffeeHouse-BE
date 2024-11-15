package com.example.Coffee.controller;

import com.example.Coffee.dto.ApiResponse;
import com.example.Coffee.model.Product;
import com.example.Coffee.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/";

    // API: Lấy danh sách sản phẩm
    @GetMapping
    public ResponseEntity<ApiResponse<List<Product>>> getAllProducts() {
        List<Product> products = productService.getAllProducts();
        if (products.isEmpty()) {
            return new ResponseEntity<>(new ApiResponse<>("Không có sản phẩm nào."), HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(new ApiResponse<>(true, "Lấy danh sách sản phẩm thành công", products), HttpStatus.OK);
    }

    // API: Tạo mới sản phẩm
    @PostMapping
    public ResponseEntity<ApiResponse<String>> createProduct(@RequestParam("name") String name,
                                                             @RequestParam("description") String description,
                                                             @RequestParam("quantity") int quantity,
                                                             @RequestParam("category") String category,
                                                             @RequestParam("image") MultipartFile image,
                                                             @RequestParam("sizeS") Double sizeS,
                                                             @RequestParam("sizeM") Double sizeM,
                                                             @RequestParam("sizeL") Double sizeL) {
        try {
            // Kiểm tra xem file ảnh có được gửi lên không
            if (image.isEmpty()) {
                return new ResponseEntity<>(new ApiResponse<>(false, "File ảnh không được để trống", null), HttpStatus.BAD_REQUEST);
            }

            // Tạo một map giá cho từng kích thước
            Map<String, Double> sizePrice = new HashMap<>();
            sizePrice.put("S", sizeS);
            sizePrice.put("M", sizeM);
            sizePrice.put("L", sizeL);

            // Gọi service để thêm sản phẩm vào cơ sở dữ liệu
            String result = productService.createProduct(name, description, quantity, sizePrice, category, image);

            if (result.equals("Sản phẩm đã được tạo thành công")) {
                return new ResponseEntity<>(new ApiResponse<>(true, result, null), HttpStatus.CREATED);
            }
            return new ResponseEntity<>(new ApiResponse<>(false, result, null), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(new ApiResponse<>("Có lỗi xảy ra khi tạo sản phẩm: " + e.getMessage()),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // API: Lấy thông tin sản phẩm theo ID
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Product>> getProductById(@PathVariable Long id) {
        Product product = productService.getProductById(id);
        if (product == null) {
            return new ResponseEntity<>(new ApiResponse<>("Không tìm thấy sản phẩm với ID: " + id), HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(new ApiResponse<>(true, "Lấy thông tin sản phẩm thành công", product), HttpStatus.OK);
    }

    // API: Xóa sản phẩm
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteProduct(@PathVariable Long id) {
        String result = productService.deleteProduct(id);
        if (result.equals("Sản phẩm đã được xóa thành công")) {
            return new ResponseEntity<>(new ApiResponse<>(true, result, null), HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(new ApiResponse<>(result), HttpStatus.NOT_FOUND);
    }

    @PostMapping("/test-upload-image")
    public ResponseEntity<ApiResponse<String>> testUploadImage(@RequestParam("image") MultipartFile image) {
        try {
            // Kiểm tra xem file ảnh có được gửi lên không
            if (image.isEmpty()) {
                return new ResponseEntity<>(new ApiResponse<>(false, "File ảnh không được để trống", null), HttpStatus.BAD_REQUEST);
            }

            // Gọi service để upload ảnh lên Imgur (hoặc xử lý theo cách khác)
            ResponseEntity<String> response = productService.uploadImage(image);
            System.out.println("check image upload: " + response);

            if (response.getStatusCode() == HttpStatus.OK) {
                return new ResponseEntity<>(new ApiResponse<>(true, "Tải ảnh lên thành công", response.getBody()), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(new ApiResponse<>(false, "Tải ảnh lên thất bại", null), HttpStatus.INTERNAL_SERVER_ERROR);
            }

        } catch (Exception e) {
            return new ResponseEntity<>(new ApiResponse<>(false, "Có lỗi xảy ra khi tải ảnh lên: " + e.getMessage(), null),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // API: Tạo mới sản phẩm với link ảnh
    @PostMapping("/create-with-link")
    public ResponseEntity<ApiResponse<String>> createProductWithLink(@RequestParam("name") String name,
                                                                     @RequestParam("description") String description,
                                                                     @RequestParam("quantity") int quantity,
                                                                     @RequestParam("category") String category,
                                                                     @RequestParam("imageUrl") String imageUrl,  // Nhận link ảnh thay vì MultipartFile
                                                                     @RequestParam("sizeS") Double sizeS,
                                                                     @RequestParam("sizeM") Double sizeM,
                                                                     @RequestParam("sizeL") Double sizeL) {
        try {
            // Kiểm tra xem link ảnh có hợp lệ không
            if (imageUrl == null || imageUrl.isEmpty()) {
                return new ResponseEntity<>(new ApiResponse<>(false, "Link ảnh không được để trống", null), HttpStatus.BAD_REQUEST);
            }

            // Tạo một map giá cho từng kích thước
            Map<String, Double> sizePrice = new HashMap<>();
            sizePrice.put("S", sizeS);
            sizePrice.put("M", sizeM);
            sizePrice.put("L", sizeL);

            // Gọi service để thêm sản phẩm vào cơ sở dữ liệu
            String result = productService.createProductWithLinkImg(name, description, quantity, sizePrice, category, imageUrl);  // Gọi service với link ảnh

            if (result.equals("Sản phẩm đã được tạo thành công")) {
                return new ResponseEntity<>(new ApiResponse<>(true, result, null), HttpStatus.CREATED);
            }
            return new ResponseEntity<>(new ApiResponse<>(false, result, null), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(new ApiResponse<>("Có lỗi xảy ra khi tạo sản phẩm: " + e.getMessage()),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
