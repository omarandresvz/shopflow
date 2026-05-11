package com.shopflow.order.controller;

import com.shopflow.order.dto.request.CreateOrderRequest;
import com.shopflow.order.dto.response.OrderResponse;
import com.shopflow.order.service.OrderService;
import com.shopflow.shared.security.model.CurrentUser;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse create(@RequestBody @Valid CreateOrderRequest request, Authentication authentication
    ) {
        CurrentUser user = (CurrentUser) authentication.getPrincipal();
        return service.create(user.userId(), request);
    }

    @GetMapping("/my")
    public List<OrderResponse> myOrders(Authentication authentication) {
        CurrentUser user = (CurrentUser) authentication.getPrincipal();
        return service.getMyOrders(user.userId());
    }
}