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
        // Tìm CartItem dựa vào ID
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm này trong giỏ hàng!"));

        // Kiểm tra nếu CartItem thuộc về giỏ hàng của user
        if (!cartItem.getCart().getUser().getId().equals(userId)) {
            throw new RuntimeException("Sản phẩm không thuộc về giỏ hàng của người dùng này");
        }

        // Lấy thông tin sản phẩm từ CartItem
        Product product = cartItem.getProduct();

        // Kiểm tra tính hợp lệ của số lượng (phải lớn hơn 0)
        if (quantity <= 0) {
            throw new RuntimeException("Số lượng sản phẩm phải lớn hơn 0");
        }

        // Kiểm tra tồn kho của sản phẩm
        if (quantity > product.getQuantity()) {
            throw new RuntimeException("Số lượng sản phẩm vượt quá tồn kho hiện tại (" + product.getQuantity() + ")");
        }

        // Cập nhật số lượng sản phẩm trong CartItem
        cartItem.setQuantity(quantity);
        cartItemRepository.save(cartItem);

        // Cập nhật thời gian chỉnh sửa giỏ hàng
        ShoppingCart cart = cartItem.getCart();
        cart.setUpdatedAt(new Date());
        shoppingCartRepository.save(cart);

        // Trả về giỏ hàng đã cập nhật
        return shoppingCartRepository.findByUserId(userId);
    }

    @Override
    public void removeCartItem(Long userId, Long cartItemId) {
        try {
            // Kiểm tra nếu CartItem tồn tại
            CartItem cartItem = cartItemRepository.findById(cartItemId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm trong giỏ hàng"));

            // Kiểm tra nếu CartItem thuộc về giỏ hàng của user
            if (!cartItem.getCart().getUser().getId().equals(userId)) {
                throw new RuntimeException("Sản phẩm không thuộc về giỏ hàng của người dùng này");
            }

            // Tiến hành xóa CartItem
            cartItemRepository.deleteById(cartItemId);
        } catch (Exception e) {
            // Xử lý lỗi: ném lỗi ra ngoài hoặc ghi lại lỗi trong log
            throw new RuntimeException("Xóa sản phẩm thất bại: " + e.getMessage());
        }
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
