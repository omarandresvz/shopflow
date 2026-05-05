package com.shopflow.order.service;

import com.shopflow.order.dto.CreateOrderRequest;
import com.shopflow.order.dto.OrderResponse;

import java.util.List;

public interface OrderService {

    OrderResponse create(Long userId, CreateOrderRequest request);

    List<OrderResponse> getMyOrders(Long userId);
}