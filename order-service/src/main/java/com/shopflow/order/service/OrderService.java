package com.shopflow.order.service;

import com.shopflow.order.dto.request.CreateOrderRequest;
import com.shopflow.order.dto.response.OrderResponse;

import java.util.List;

public interface OrderService {

    OrderResponse create(Long userId, CreateOrderRequest request);

    List<OrderResponse> getMyOrders(Long userId);

    void payOrder(Long orderId);

    void shipOrder(Long orderId);

    void deliverOrder(Long orderId);

    void cancelOrder(Long orderId);
}