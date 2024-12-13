package com.example.Coffee.service.serviceImpl;

import com.example.Coffee.dto.OrderItemRequest;
import com.example.Coffee.dto.OrderItemResponse;
import com.example.Coffee.dto.OrderResponse;
import com.example.Coffee.model.*;
import com.example.Coffee.model.enums.OrderStatus;
import com.example.Coffee.model.enums.PaymentMethod;
import com.example.Coffee.repository.*;
import com.example.Coffee.service.OrderService;
import com.example.Coffee.service.ShoppingCartService;
import com.example.Coffee.utils.DateUtils;
import com.example.Coffee.webSocket.WebSocketHandlerImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private ShoppingCartRepository shoppingCartRepository;

    @Autowired
    private ShoppingCartService shoppingCartService;

    @Autowired
    private WebSocketHandlerImpl webSocketHandler;

    @Transactional
    @Override
    public OrderResponse createOrder(Long userId, List<OrderItemRequest> orderItemRequests) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với ID: " + userId));

        if (orderItemRequests.isEmpty()) {
            throw new RuntimeException("Danh sách sản phẩm đặt hàng không được để trống.");
        }

        PaymentMethod paymentMethod = orderItemRequests.getFirst().getPaymentMethod();

        // Tìm ShoppingCart của người dùng
        ShoppingCart shoppingCart = shoppingCartRepository.findByUserId(userId);
        if (shoppingCart == null) {
            throw new RuntimeException("Không tìm thấy giỏ hàng của người dùng này");
        }

        Order order = new Order();
        order.setUser(user);
        order.setStatus(OrderStatus.PENDING);
        order.setCreatedAt(DateUtils.addHoursToDate(new Date(), 7));
        order.setUpdatedAt(DateUtils.addHoursToDate(new Date(), 7));
        order.setPaid(false);
        order.setPaymentMethod(paymentMethod);

        List<OrderItem> orderItems = new ArrayList<>();
        double totalPrice = 0;

        for (OrderItemRequest itemRequest : orderItemRequests) {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với ID: " + itemRequest.getProductId()));

            Double price = product.getSizePrice().get(itemRequest.getSize());
            if (price == null) {
                throw new RuntimeException("Kích thước sản phẩm không hợp lệ: " + itemRequest.getSize());
            }

            // Kiểm tra số lượng sản phẩm trong kho có đủ không
            if (product.getQuantity() < itemRequest.getQuantity()) {
                throw new RuntimeException("Số lượng sản phẩm không đủ: " + product.getName());
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setSize(itemRequest.getSize());
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setPrice(price);
            orderItemRepository.save(orderItem);

            orderItems.add(orderItem);
            totalPrice += price * itemRequest.getQuantity();

            // Xóa sản phẩm khỏi giỏ hàng bằng phương thức removeCartItem
            shoppingCartService.removeCartItem(userId, itemRequest.getCartItemId());

            // Trừ số lượng sản phẩm trong kho
            product.setQuantity(product.getQuantity() - itemRequest.getQuantity());
            productRepository.save(product);
        }

        order.setOrderItems(orderItems);
        order.setTotalPrice(totalPrice);
        orderRepository.save(order);

        List<OrderItemResponse> itemResponses = new ArrayList<>();
        for (OrderItem item : orderItems) {
            itemResponses.add(new OrderItemResponse(
                    item.getProduct().getName(),
                    item.getSize(),
                    item.getQuantity(),
                    item.getPrice(),
                    item.getProduct().getImageUrl() // Thêm URL hình ảnh sản phẩm
            ));
        }

        return new OrderResponse(order.getId(), totalPrice, order.getStatus(), order.getCreatedAt(), order.getUpdatedAt(), itemResponses, order.getPaymentMethod());
    }

    @Override
    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, OrderStatus status, Long userId) {
        // Lấy thông tin đơn hàng từ ID
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng với ID: " + orderId));

        // Kiểm tra thứ tự chuyển trạng thái
        if (order.getStatus() == OrderStatus.DELIVERED || order.getStatus() == OrderStatus.CANCELED) {
            throw new IllegalArgumentException("Không thể thay đổi trạng thái đơn hàng khi đã giao hoặc đã hủy.");
        }

        if (order.getStatus() == OrderStatus.PENDING && status != OrderStatus.PROCESSING) {
            throw new IllegalArgumentException("Chỉ có thể chuyển trạng thái từ PENDING sang PROCESSING.");
        }

        if (order.getStatus() == OrderStatus.PROCESSING && status != OrderStatus.SHIPPED) {
            throw new IllegalArgumentException("Chỉ có thể chuyển trạng thái từ PROCESSING sang SHIPPED.");
        }

        if (order.getStatus() == OrderStatus.SHIPPED && status != OrderStatus.DELIVERED) {
            throw new IllegalArgumentException("Chỉ có thể chuyển trạng thái từ SHIPPED sang DELIVERED.");
        }

        // Cập nhật trạng thái đơn hàng
        order.setStatus(status);
        order.setUpdatedAt(DateUtils.addHoursToDate(new Date(), 7));
        orderRepository.save(order);

        // Chuẩn bị dữ liệu trả về
        List<OrderItemResponse> itemResponses = new ArrayList<>();
        for (OrderItem item : order.getOrderItems()) {
            itemResponses.add(new OrderItemResponse(
                    item.getProduct().getName(),
                    item.getSize(),
                    item.getQuantity(),
                    item.getPrice(),
                    item.getProduct().getImageUrl()
            ));
        }

        OrderResponse orderResponse = new OrderResponse(
                order.getId(),
                order.getTotalPrice(),
                order.getStatus(),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                itemResponses,
                order.getPaymentMethod()
        );

        // Cập nhật trạng thái đơn hàng và gửi thông báo qua WebSocket
        webSocketHandler.sendOrderStatusUpdate(userId.toString(), orderResponse);

        return orderResponse;
    }

    @Override
    @Transactional
    public OrderResponse cancelOrder(Long orderId) {
        // Lấy thông tin đơn hàng từ ID
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng với ID: " + orderId));

        // Kiểm tra điều kiện hủy đơn hàng
        if (order.getStatus() == OrderStatus.PROCESSING || order.getStatus() == OrderStatus.SHIPPED || order.getStatus() == OrderStatus.DELIVERED) {
            throw new IllegalArgumentException("Không thể hủy đơn hàng khi đã xác nhận hoặc đã giao hoặc đang vận chuyển.");
        }

        if (order.getStatus() == OrderStatus.CANCELED) {
            throw new IllegalArgumentException("Đơn hàng này đã bị hủy trước đó.");
        }

        // Cập nhật trạng thái đơn hàng thành CANCELED
        order.setStatus(OrderStatus.CANCELED);
        order.setUpdatedAt(DateUtils.addHoursToDate(new Date(), 7));
        orderRepository.save(order);

        // Hoàn trả số lượng sản phẩm
        for (OrderItem item : order.getOrderItems()) {
            Product product = item.getProduct();
            product.setQuantity(product.getQuantity() + item.getQuantity()); // Trả lại số lượng
            productRepository.save(product); // Lưu lại thay đổi trong database
        }

        // Chuẩn bị dữ liệu trả về
        List<OrderItemResponse> itemResponses = new ArrayList<>();
        for (OrderItem item : order.getOrderItems()) {
            itemResponses.add(new OrderItemResponse(
                    item.getProduct().getName(),
                    item.getSize(),
                    item.getQuantity(),
                    item.getPrice(),
                    item.getProduct().getImageUrl()
            ));
        }

        return new OrderResponse(
                order.getId(),
                order.getTotalPrice(),
                order.getStatus(),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                itemResponses,
                order.getPaymentMethod()
        );
    }

    @Override
    public List<OrderResponse> getUserOrders(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với ID: " + userId));

        List<Order> orders = orderRepository.findByUser(user);

        // Sắp xếp danh sách orders theo updatedAt (mới nhất lên đầu)
        orders.sort((o1, o2) -> o2.getUpdatedAt().compareTo(o1.getUpdatedAt()));

        List<OrderResponse> orderResponses = new ArrayList<>();

        for (Order order : orders) {
            List<OrderItemResponse> itemResponses = new ArrayList<>();
            for (OrderItem item : order.getOrderItems()) {
                itemResponses.add(new OrderItemResponse(
                        item.getProduct().getName(),
                        item.getSize(),
                        item.getQuantity(),
                        item.getPrice(),
                        item.getProduct().getImageUrl() // Thêm URL hình ảnh sản phẩm
                ));
            }

            orderResponses.add(new OrderResponse(
                    order.getId(),
                    order.getTotalPrice(),
                    order.getStatus(),
                    order.getCreatedAt(), // Thêm thời gian tạo đơn hàng
                    order.getUpdatedAt(),
                    itemResponses,
                    order.getPaymentMethod()
            ));
        }

        return orderResponses;
    }
}
