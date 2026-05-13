package com.shopflow.order.controller;

import com.shopflow.order.dto.request.CreateOrderRequest;
import com.shopflow.order.dto.response.OrderResponse;
import com.shopflow.order.service.OrderService;
import com.shopflow.shared.security.model.CurrentUser;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping( value = "/api/v1/orders", produces = "application/json")
@RequiredArgsConstructor
@Tag(
        name = "Órdenes",
        description = "Endpoints para creación, consulta y flujo de estados de órdenes"
)
public class OrderController {

    private final OrderService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Crear orden",
            description = "Crea una nueva orden y reduce automáticamente el stock de productos"
    )
    @ApiResponse(responseCode = "201", description = "Orden creada exitosamente")
    @ApiResponse(responseCode = "400", description = "Error de validación")
    @ApiResponse(responseCode = "401", description = "No autenticado")
    @ApiResponse(responseCode = "409", description = "Stock insuficiente")
    public OrderResponse create(
            @RequestBody @Valid CreateOrderRequest request,
            Authentication authentication
    ) {
        CurrentUser user = (CurrentUser) authentication.getPrincipal();
        return service.create(user.userId(), request);
    }

    @GetMapping("/my")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Obtener mis órdenes",
            description = "Obtiene todas las órdenes del usuario autenticado"
    )
    @ApiResponse(responseCode = "200", description = "Órdenes obtenidas exitosamente")
    @ApiResponse(responseCode = "401", description = "No autenticado")
    public List<OrderResponse> myOrders(Authentication authentication) {
        CurrentUser user = (CurrentUser) authentication.getPrincipal();
        return service.getMyOrders(user.userId());
    }

    @PatchMapping("/{id}/pay")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Marcar orden como pagada",
            description = "Cambia el estado de una orden de CREATED a PAID. Requiere rol ADMIN"
    )
    @ApiResponse(responseCode = "204", description = "Orden pagada exitosamente")
    @ApiResponse(responseCode = "401", description = "No autenticado")
    @ApiResponse(responseCode = "403", description = "Acceso denegado")
    @ApiResponse(responseCode = "409", description = "Transición de estado inválida")
    public void payOrder(@PathVariable Long id) {
        service.payOrder(id);
    }

    @PatchMapping("/{id}/ship")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Despachar orden",
            description = "Cambia el estado de una orden de PAID a SHIPPED. Requiere rol ADMIN"
    )
    @ApiResponse(responseCode = "204", description = "Orden despachada exitosamente")
    @ApiResponse(responseCode = "401", description = "No autenticado")
    @ApiResponse(responseCode = "403", description = "Acceso denegado")
    @ApiResponse(responseCode = "409", description = "Transición de estado inválida")
    public void shipOrder(@PathVariable Long id) {
        service.shipOrder(id);
    }

    @PatchMapping("/{id}/deliver")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Entregar orden",
            description = "Cambia el estado de una orden de SHIPPED a DELIVERED. Requiere rol ADMIN"
    )
    @ApiResponse(responseCode = "204", description = "Orden entregada exitosamente")
    @ApiResponse(responseCode = "401", description = "No autenticado")
    @ApiResponse(responseCode = "403", description = "Acceso denegado")
    @ApiResponse(responseCode = "409", description = "Transición de estado inválida")
    public void deliverOrder(@PathVariable Long id) {
        service.deliverOrder(id);
    }

    @PatchMapping("/{id}/cancel")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Cancelar orden",
            description = "Cancela una orden y restaura automáticamente el stock de productos"
    )
    @ApiResponse(responseCode = "204", description = "Orden cancelada exitosamente")
    @ApiResponse(responseCode = "401", description = "No autenticado")
    @ApiResponse(responseCode = "403", description = "No tienes permisos para operar esta orden")
    @ApiResponse(responseCode = "409", description = "Transición de estado inválida")
    public void cancelOrder(@PathVariable Long id) {
        service.cancelOrder(id);
    }
}