package com.shopflow.order.service.impl;

import com.shopflow.order.client.ProductClient;
import com.shopflow.order.dto.CreateOrderRequest;
import com.shopflow.order.dto.OrderItemResponse;
import com.shopflow.order.dto.OrderResponse;
import com.shopflow.order.dto.ProductResponse;
import com.shopflow.order.entity.Order;
import com.shopflow.order.entity.OrderItem;
import com.shopflow.order.entity.OrderStatus;
import com.shopflow.order.repository.OrderRepository;
import com.shopflow.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository repository;
    private final ProductClient productClient;

    @Override
    @Transactional
    public OrderResponse create(Long userId, CreateOrderRequest request) {
        Order order = Order.builder()
                .userId(userId)
                .status(OrderStatus.CREATED)
                .createdAt(LocalDateTime.now())
                .total(0.0)
                .build();

        double total = 0.0;

        for (var itemRequest : request.items()) {
            ProductResponse product = productClient.getProductById(itemRequest.productId());

            double subtotal = product.price() * itemRequest.quantity();

            OrderItem item = OrderItem.builder()
                    .productId(product.id())
                    .productName(product.name())
                    .unitPrice(product.price())
                    .quantity(itemRequest.quantity())
                    .subtotal(subtotal)
                    .build();

            order.addItem(item);

            productClient.decreaseStock(product.id(), itemRequest.quantity());

            total += subtotal;
        }

        order.setTotal(total);

        Order saved = repository.save(order);

        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getMyOrders(Long userId) {
        return repository.findByUserId(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private OrderResponse mapToResponse(Order order) {
        List<OrderItemResponse> items = order.getItems()
                .stream()
                .map(item -> new OrderItemResponse(
                        item.getProductId(),
                        item.getProductName(),
                        item.getUnitPrice(),
                        item.getQuantity(),
                        item.getSubtotal()
                ))
                .toList();

        return new OrderResponse(
                order.getId(),
                order.getTotal(),
                order.getStatus().name(),
                order.getCreatedAt(),
                items
        );
    }
}