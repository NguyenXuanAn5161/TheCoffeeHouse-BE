package com.example.Coffee.service.serviceImpl;

import com.example.Coffee.dto.OrderItemRequest;
import com.example.Coffee.dto.OrderItemResponse;
import com.example.Coffee.dto.OrderResponse;
import com.example.Coffee.model.*;
import com.example.Coffee.model.enums.OrderStatus;
import com.example.Coffee.repository.*;
import com.example.Coffee.service.OrderService;
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

    @Transactional
    @Override
    public OrderResponse createOrder(Long userId, List<OrderItemRequest> orderItemRequests) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với ID: " + userId));

        if (orderItemRequests.isEmpty()) {
            throw new RuntimeException("Danh sách sản phẩm đặt hàng không được để trống.");
        }

        Order order = new Order();
        order.setUser(user);
        order.setStatus(OrderStatus.PENDING);
        order.setCreatedAt(new Date());
        order.setUpdatedAt(new Date());
        order.setPaid(false);

        List<OrderItem> orderItems = new ArrayList<>();
        double totalPrice = 0;

        for (OrderItemRequest itemRequest : orderItemRequests) {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với ID: " + itemRequest.getProductId()));

            Double price = product.getSizePrice().get(itemRequest.getSize());
            if (price == null) {
                throw new RuntimeException("Kích thước sản phẩm không hợp lệ: " + itemRequest.getSize());
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
                    item.getPrice()
            ));
        }

        return new OrderResponse(order.getId(), totalPrice, order.getStatus(), itemResponses);
    }

    @Override
    public List<OrderResponse> getUserOrders(Long userId) {
        // Lấy người dùng từ userId
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với ID: " + userId));

        // Lấy tất cả đơn hàng của người dùng
        List<Order> orders = orderRepository.findByUser(user);

        // Chuyển đổi đơn hàng thành OrderResponse để trả về cho người dùng
        List<OrderResponse> orderResponses = new ArrayList<>();
        for (Order order : orders) {
            List<OrderItemResponse> itemResponses = new ArrayList<>();
            for (OrderItem item : order.getOrderItems()) {
                itemResponses.add(new OrderItemResponse(
                        item.getProduct().getName(),
                        item.getSize(),
                        item.getQuantity(),
                        item.getPrice()
                ));
            }
            orderResponses.add(new OrderResponse(
                    order.getId(),
                    order.getTotalPrice(),
                    order.getStatus(),
                    itemResponses
            ));
        }

        return orderResponses;
    }
}
