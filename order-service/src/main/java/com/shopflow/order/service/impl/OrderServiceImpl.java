package com.shopflow.order.service.impl;

import com.shopflow.order.dto.CreateOrderRequest;
import com.shopflow.order.dto.OrderResponse;
import com.shopflow.order.entity.Order;
import com.shopflow.order.entity.OrderStatus;
import com.shopflow.order.repository.OrderRepository;
import com.shopflow.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository repository;

    @Override
    public OrderResponse create(Long userId, CreateOrderRequest request) {

        Order order = Order.builder()
                .userId(userId)
                .total(request.total())
                .status(OrderStatus.CREATED)
                .createdAt(LocalDateTime.now())
                .build();

        Order saved = repository.save(order);

        return new OrderResponse(
                saved.getId(),
                saved.getTotal(),
                saved.getStatus().name(),
                saved.getCreatedAt()
        );
    }

    @Override
    public List<OrderResponse> getMyOrders(Long userId) {
        return repository.findByUserId(userId)
                .stream()
                .map(o -> new OrderResponse(
                        o.getId(),
                        o.getTotal(),
                        o.getStatus().name(),
                        o.getCreatedAt()
                ))
                .toList();
    }
}