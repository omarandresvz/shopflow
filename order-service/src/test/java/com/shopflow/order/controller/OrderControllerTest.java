package com.shopflow.order.controller;

import com.shopflow.order.dto.request.CreateOrderRequest;
import com.shopflow.order.dto.request.OrderItemRequest;
import com.shopflow.order.dto.response.OrderItemResponse;
import com.shopflow.order.dto.response.OrderResponse;
import com.shopflow.order.service.OrderService;
import com.shopflow.shared.exception.ErrorResponseFactory;
import com.shopflow.shared.security.filter.JwtAuthenticationFilter;
import com.shopflow.shared.security.model.CurrentUser;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
@AutoConfigureMockMvc(addFilters = false)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrderController controller;

    @MockitoBean
    private OrderService service;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private AuthenticationEntryPoint authenticationEntryPoint;

    @MockitoBean
    private AccessDeniedHandler accessDeniedHandler;

    @MockitoBean
    private ErrorResponseFactory errorResponseFactory;

    @Test
    void shouldCreateOrderSuccessfully() {
        CurrentUser currentUser = new CurrentUser(
                10L,
                "customer@test.com",
                "CUSTOMER"
        );

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                currentUser,
                null,
                List.of()
        );

        CreateOrderRequest request = new CreateOrderRequest(
                List.of(new OrderItemRequest(1L, 2))
        );

        OrderResponse response = buildOrderResponse();

        when(service.create(eq(10L), any(CreateOrderRequest.class))).thenReturn(response);

        OrderResponse result = controller.create(request, authentication);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.status()).isEqualTo("CREATED");
        assertThat(result.total()).isEqualTo(2000.0);
        assertThat(result.items()).hasSize(1);
        assertThat(result.items().get(0).productId()).isEqualTo(1L);
        assertThat(result.items().get(0).quantity()).isEqualTo(2);

        verify(service).create(eq(10L), any(CreateOrderRequest.class));
    }

    @Test
    void shouldGetMyOrdersSuccessfully() {
        CurrentUser currentUser = new CurrentUser(
                10L,
                "customer@test.com",
                "CUSTOMER"
        );

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                currentUser,
                null,
                List.of()
        );

        when(service.getMyOrders(10L)).thenReturn(List.of(buildOrderResponse()));

        List<OrderResponse> result = controller.myOrders(authentication);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(1L);
        assertThat(result.get(0).status()).isEqualTo("CREATED");

        verify(service).getMyOrders(10L);
    }

    @Test
    void shouldPayOrderSuccessfully() throws Exception {
        doNothing().when(service).payOrder(1L);

        mockMvc.perform(patch("/api/v1/orders/1/pay"))
                .andExpect(status().isNoContent());

        verify(service).payOrder(1L);
    }

    @Test
    void shouldShipOrderSuccessfully() throws Exception {
        doNothing().when(service).shipOrder(1L);

        mockMvc.perform(patch("/api/v1/orders/1/ship"))
                .andExpect(status().isNoContent());

        verify(service).shipOrder(1L);
    }

    @Test
    void shouldDeliverOrderSuccessfully() throws Exception {
        doNothing().when(service).deliverOrder(1L);

        mockMvc.perform(patch("/api/v1/orders/1/deliver"))
                .andExpect(status().isNoContent());

        verify(service).deliverOrder(1L);
    }

    @Test
    void shouldCancelOrderSuccessfully() throws Exception {
        doNothing().when(service).cancelOrder(1L);

        mockMvc.perform(patch("/api/v1/orders/1/cancel"))
                .andExpect(status().isNoContent());

        verify(service).cancelOrder(1L);
    }

    private OrderResponse buildOrderResponse() {
        OrderItemResponse item = new OrderItemResponse(
                1L,
                "Notebook",
                1000.0,
                2,
                2000.0
        );

        return new OrderResponse(
                1L,
                2000.0,
                "CREATED",
                LocalDateTime.now(),
                List.of(item)
        );
    }
}