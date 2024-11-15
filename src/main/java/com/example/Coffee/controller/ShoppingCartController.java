package com.example.Coffee.controller;

import com.example.Coffee.dto.CartItemDto;
import com.example.Coffee.dto.ShoppingCartDto;
import com.example.Coffee.model.ShoppingCart;
import com.example.Coffee.dto.ApiResponse;
import com.example.Coffee.service.ShoppingCartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    // API: Lấy giỏ hàng của người dùng theo userId
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<ShoppingCartDto>> getCart(@PathVariable Long userId) {
        try {
            ShoppingCart cart = shoppingCartService.getCartByUserId(userId);

            // Lấy userId từ ShoppingCart
            Long userIdFromCart = (cart.getUser() != null) ? cart.getUser().getId() : null;

            // Chuyển đổi từ List<CartItem> sang List<CartItemDto>
            List<CartItemDto> cartItemsDto = cart.getItems().stream()
                    .map(item -> new CartItemDto(
                            item.getId(),
                            item.getProduct().getImageUrl(),
                            item.getProduct().getId(),
                            item.getProduct().getName(),
                            item.getSize(),
                            item.getQuantity(),
                            item.getPrice()
                    ))
                    .toList();

            // Tạo ShoppingCartDto
            ShoppingCartDto shoppingCartDto = new ShoppingCartDto(userIdFromCart, cartItemsDto);

            // Trả về ApiResponse với ShoppingCartDto
            return ResponseEntity.ok(new ApiResponse<>(true, "Lấy giỏ hàng thành công", shoppingCartDto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "không tìm thấy", null));
        }
    }

    // API: Thêm sản phẩm vào giỏ hàng sử dụng RequestParam
    @PostMapping("/{userId}/add")
    public ResponseEntity<ApiResponse<ShoppingCart>> addItem(
            @PathVariable Long userId,
            @RequestParam("productId") Long productId,
            @RequestParam("size") String size,
            @RequestParam("quantity") int quantity) {
        try {
            // Gọi service để thêm item vào giỏ hàng
            shoppingCartService.addItemToCart(userId, productId, size, quantity);

            // Trả về kết quả thành công
            return ResponseEntity.ok(new ApiResponse<>(true, "Thêm thành công", null));
        } catch (Exception e) {
            // Trả về lỗi nếu có
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }


    // API: Cập nhật số lượng sản phẩm trong giỏ hàng sử dụng RequestParam
    @PutMapping("/{userId}/update/{itemId}")
    public ResponseEntity<ApiResponse<ShoppingCart>> updateItem(
            @PathVariable Long userId,
            @PathVariable Long itemId,
            @RequestParam("quantity") int quantity) {

        ShoppingCart updatedCart = shoppingCartService.updateCartItem(userId, itemId, quantity);
        return ResponseEntity.ok(new ApiResponse<>(true, "Cập nhật số lượng sản phẩm thành công", updatedCart));
    }

    // API: Xóa một sản phẩm khỏi giỏ hàng
    @DeleteMapping("/{userId}/remove/{itemId}")
    public ResponseEntity<ApiResponse<String>> removeItem(@PathVariable Long userId, @PathVariable Long itemId) {
        shoppingCartService.removeCartItem(userId, itemId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Sản phẩm đã được xóa khỏi giỏ hàng", null));
    }

    // API: Xóa toàn bộ giỏ hàng
    @DeleteMapping("/{userId}/clear")
    public ResponseEntity<ApiResponse<String>> clearCart(@PathVariable Long userId) {
        shoppingCartService.clearCart(userId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Giỏ hàng đã được xóa", null));
    }
}

