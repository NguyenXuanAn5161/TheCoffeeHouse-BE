package com.example.Coffee.controller;

import com.example.Coffee.dto.ApiResponse;
import com.example.Coffee.dto.filter.ProductFilterDto;
import com.example.Coffee.model.Product;
import com.example.Coffee.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    // API: lấy danh sách sản phẩm theo filter
    @GetMapping
    public ResponseEntity<ApiResponse<List<Product>>> getProductFilter(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "isNew", required = false) Boolean isNew) {
        try {
            // Tạo đối tượng Pageable với số trang và kích thước trang
            Pageable pageable = PageRequest.of(page, size);

            // Tạo đối tượng filter từ query parameters
            ProductFilterDto filter = new ProductFilterDto();
            filter.setName(name);
            filter.setCategory(category);
            filter.setIsNew(isNew);

            Page<Product> products = productService.getProducts(filter, pageable);

            System.out.println("products: " + products.getContent().isEmpty());

            if (products.getContent().isEmpty()) {
                return new ResponseEntity<>(new ApiResponse<>(true, "Không có sản phẩm nào.",  products.getContent(), products.getTotalPages(), products.getTotalElements()), HttpStatus.OK);
            }
            return new ResponseEntity<>(new ApiResponse<>(true, "Lấy danh sách sản phẩm thành công!", products.getContent(), products.getTotalPages(), products.getTotalElements()), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(new ApiResponse<>(false, "Có lỗi xảy ra khi lọc sản phẩm: " + e.getMessage(), null),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
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
                                                             @RequestParam("sizeL") Double sizeL,
                                                             @RequestParam(value = "isNew", required = false, defaultValue = "false") Boolean isNew,
                                                             @RequestParam(value = "newUntil", required = false) String newUntilStr) {
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

            // Chuyển đổi `newUntilStr` (String) thành `Date`
            Date newUntil = null;
            if (isNew) {
                if (newUntilStr != null && !newUntilStr.isEmpty()) {
                    newUntil = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(newUntilStr);
                }
            }

            // Gọi service để thêm sản phẩm vào cơ sở dữ liệu
            String result = productService.createProduct(name, description, quantity, sizePrice, category, image, isNew, newUntil);

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
                                                                     @RequestParam("sizeL") Double sizeL,
                                                                     @RequestParam(value = "isNew", required = false, defaultValue = "false") Boolean isNew,
                                                                     @RequestParam(value = "newUntil", required = false) String newUntilStr) {
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

            // Chuyển đổi `newUntilStr` (String) thành `Date`
            Date newUntil = null;
            if (isNew) {
                if (newUntilStr != null && !newUntilStr.isEmpty()) {
                    newUntil = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(newUntilStr);
                }
            }

            // Gọi service để thêm sản phẩm vào cơ sở dữ liệu
            String result = productService.createProductWithLinkImg(name, description, quantity, sizePrice, category, imageUrl, isNew, newUntil);  // Gọi service với link ảnh

            if (result.equals("Sản phẩm đã được tạo thành công")) {
                return new ResponseEntity<>(new ApiResponse<>(true, result, null), HttpStatus.CREATED);
            }
            return new ResponseEntity<>(new ApiResponse<>(false, result, null), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(new ApiResponse<>("Có lỗi xảy ra khi tạo sản phẩm: " + e.getMessage()),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/addFromFile")
    public ResponseEntity<ApiResponse<String>> addProductFromFile(@RequestParam("file") MultipartFile file) {
        try {
            // Kiểm tra nếu file có tên hợp lệ
            if (file.isEmpty() || file.getOriginalFilename() == null) {
                return new ResponseEntity<>(new ApiResponse<>(false, "Không có tệp nào được tải lên", null), HttpStatus.BAD_REQUEST);
            }

            // Gọi service để thêm sản phẩm từ dữ liệu
            ResponseEntity<String> result = productService.addProductsFromFile(file);

            // Trả lại danh sách sản phẩm đã được thêm vào cơ sở dữ liệu
            if (result.getStatusCode() == HttpStatus.OK) {
                return new ResponseEntity<>(new ApiResponse<>(true, "Danh sách đã được thêm thành công!", null), HttpStatus.CREATED);
            }
            return new ResponseEntity<>(new ApiResponse<>(false, "Thêm danh sách thất bại!", null), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (IOException e) {
            return new ResponseEntity<>(new ApiResponse<>(false, "Có lỗi xảy ra khi thêm danh sách sản phẩm: " + e.getMessage(), null),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
