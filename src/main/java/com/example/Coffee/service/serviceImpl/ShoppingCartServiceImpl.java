package com.example.Coffee.service.serviceImpl;

import com.example.Coffee.dto.CartItemDto;
import com.example.Coffee.model.CartItem;
import com.example.Coffee.model.Product;
import com.example.Coffee.model.ShoppingCart;
import com.example.Coffee.model.User;
import com.example.Coffee.repository.CartItemRepository;
import com.example.Coffee.repository.ProductRepository;
import com.example.Coffee.repository.ShoppingCartRepository;
import com.example.Coffee.repository.UserRepository;
import com.example.Coffee.service.ShoppingCartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;

@Service
public class ShoppingCartServiceImpl implements ShoppingCartService {
    @Autowired
    private ShoppingCartRepository shoppingCartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private UserRepository userRepository;

    @Override
    public ShoppingCart getCartByUserId(Long userId) {
        return shoppingCartRepository.findByUserId(userId);
    }

    @Override
    public ShoppingCart addItemToCart(Long userId, Long productId, String size, int quantity) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new RuntimeException("Không tìm thấy người dùng này");
        }

        User user = userOptional.get();

        // Tìm ShoppingCart của người dùng hoặc tạo mới nếu chưa có
        ShoppingCart cart = shoppingCartRepository.findByUserId(userId);
        if (cart == null) {
            cart = new ShoppingCart();
            cart.setUser(user);
            cart.setItems(new ArrayList<>());
            cart.setCreatedAt(new Date());
            cart.setUpdatedAt(new Date());

            // Lưu ShoppingCart trước khi thêm CartItem
            shoppingCartRepository.save(cart);
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

        // Tìm kiếm CartItem cùng sản phẩm và size trong giỏ hàng
        CartItem existingItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId) && item.getSize().equals(size))
                .findFirst()
                .orElse(null);

        if (existingItem != null) {
            // Nếu đã tồn tại, cộng dồn số lượng
            existingItem.setQuantity(existingItem.getQuantity() + quantity);
            cartItemRepository.save(existingItem);
        } else {
            // Nếu chưa tồn tại, tạo mới CartItem
            CartItem newItem = new CartItem();
            newItem.setProduct(product);
            newItem.setSize(size);
            newItem.setQuantity(quantity);
            newItem.setPrice(product.getSizePrice().get(size));
            newItem.setCart(cart);  // Set liên kết với ShoppingCart đã được lưu

            // Lưu CartItem mới
            cartItemRepository.save(newItem);

            // Thêm CartItem vào danh sách items của ShoppingCart
            cart.getItems().add(newItem);
        }

        // Cập nhật thời gian của ShoppingCart
        cart.setUpdatedAt(new Date());
        shoppingCartRepository.save(cart);

        return cart;
    }

    @Override
    public ShoppingCart updateCartItem(Long userId, Long cartItemId, int quantity) {
        CartItem cartItem = cartItemRepository.findById(cartItemId).orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm này!"));
        cartItem.setQuantity(quantity);
        cartItemRepository.save(cartItem);

        return shoppingCartRepository.findByUserId(userId);
    }

    @Override
    public void removeCartItem(Long userId, Long cartItemId) {
        cartItemRepository.deleteById(cartItemId);
    }

    @Override
    public void clearCart(Long userId) {
        // Tìm ShoppingCart của người dùng
        ShoppingCart shoppingCart = shoppingCartRepository.findByUserId(userId);

//        System.out.println("check: " + shoppingCart);

        if (shoppingCart == null) {
            throw new RuntimeException("Không tìm thấy giỏ hàng của người dùng này");
        }

        // Xóa toàn bộ CartItem liên quan đến ShoppingCart
        cartItemRepository.deleteAll(shoppingCart.getItems());

        // Xóa danh sách items khỏi ShoppingCart và cập nhật thời gian
        shoppingCart.getItems().clear();
        shoppingCart.setUpdatedAt(new Date());

        // Lưu ShoppingCart sau khi xóa toàn bộ CartItem
        shoppingCartRepository.save(shoppingCart);
    }

}
