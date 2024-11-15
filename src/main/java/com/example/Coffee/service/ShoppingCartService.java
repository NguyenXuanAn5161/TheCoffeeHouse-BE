package com.example.Coffee.service;

import com.example.Coffee.dto.CartItemDto;
import com.example.Coffee.model.ShoppingCart;

public interface ShoppingCartService {
    ShoppingCart getCartByUserId(Long userId);
    ShoppingCart addItemToCart(Long userId,Long productId,String size, int quantity);
    ShoppingCart updateCartItem(Long userId, Long cartItemId, int quantity);
    void removeCartItem(Long userId, Long cartItemId);
    void clearCart(Long userId);
}
