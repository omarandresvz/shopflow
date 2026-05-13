package com.shopflow.product.controller;

import com.shopflow.product.dto.request.ProductRequest;
import com.shopflow.product.dto.request.UpdateStockRequest;
import com.shopflow.product.dto.response.ProductResponse;
import com.shopflow.product.service.ProductService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/products", produces = "application/json")
@RequiredArgsConstructor
@Tag(
        name = "Productos",
        description = "Endpoints para gestión de productos y operaciones de stock"
)
public class ProductController {

    private final ProductService service;

    @GetMapping
    @Operation(
            summary = "Listar productos",
            description = "Obtiene todos los productos activos disponibles"
    )
    @ApiResponse(responseCode = "200", description = "Productos obtenidos exitosamente")
    public List<ProductResponse> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Obtener producto por ID",
            description = "Obtiene el detalle de un producto activo mediante su ID"
    )
    @ApiResponse(responseCode = "200", description = "Producto obtenido exitosamente")
    @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    public ProductResponse findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Crear producto",
            description = "Crea un nuevo producto. Requiere rol ADMIN"
    )
    @ApiResponse(responseCode = "201", description = "Producto creado exitosamente")
    @ApiResponse(responseCode = "400", description = "Error de validación")
    @ApiResponse(responseCode = "401", description = "No autenticado")
    @ApiResponse(responseCode = "403", description = "Acceso denegado")
    public ProductResponse create(@RequestBody @Valid ProductRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Actualizar producto",
            description = "Actualiza los datos de un producto existente. Requiere rol ADMIN"
    )
    @ApiResponse(responseCode = "200", description = "Producto actualizado exitosamente")
    @ApiResponse(responseCode = "400", description = "Error de validación")
    @ApiResponse(responseCode = "401", description = "No autenticado")
    @ApiResponse(responseCode = "403", description = "Acceso denegado")
    @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    public ProductResponse update(
            @PathVariable Long id,
            @RequestBody @Valid ProductRequest request
    ) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Eliminar producto",
            description = "Elimina lógicamente un producto. Requiere rol ADMIN"
    )
    @ApiResponse(responseCode = "204", description = "Producto eliminado exitosamente")
    @ApiResponse(responseCode = "401", description = "No autenticado")
    @ApiResponse(responseCode = "403", description = "Acceso denegado")
    @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    @PatchMapping("/{id}/stock/decrease")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Disminuir stock",
            description = "Disminuye el stock de un producto. Endpoint interno utilizado por order-service al crear órdenes"
    )
    @ApiResponse(responseCode = "204", description = "Stock disminuido exitosamente")
    @ApiResponse(responseCode = "400", description = "Cantidad inválida")
    @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    @ApiResponse(responseCode = "409", description = "Stock insuficiente")
    public void decreaseStock(
            @PathVariable Long id,
            @RequestBody @Valid UpdateStockRequest request
    ) {
        service.decreaseStock(id, request.quantity());
    }

    @PatchMapping("/{id}/stock/increase")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Aumentar stock",
            description = "Aumenta el stock de un producto. Endpoint interno utilizado por order-service al cancelar órdenes"
    )
    @ApiResponse(responseCode = "204", description = "Stock aumentado exitosamente")
    @ApiResponse(responseCode = "400", description = "Cantidad inválida")
    @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    public void increaseStock(
            @PathVariable Long id,
            @RequestBody @Valid UpdateStockRequest request
    ) {
        service.increaseStock(id, request.quantity());
    }
}