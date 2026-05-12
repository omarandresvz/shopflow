package com.shopflow.product.controller;

import com.shopflow.product.dto.request.ProductRequest;
import com.shopflow.product.dto.request.UpdateStockRequest;
import com.shopflow.product.dto.response.ProductResponse;
import com.shopflow.product.service.ProductService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService service;

    // Público
    @GetMapping
    public List<ProductResponse> findAll() {
        return service.findAll();
    }

    // Público
    @GetMapping("/{id}")
    public ProductResponse findById(@PathVariable Long id) {
        return service.findById(id);
    }

    // ADMIN
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse create(@RequestBody @Valid ProductRequest request) {
        return service.create(request);
    }

    // ADMIN
    @PutMapping("/{id}")
    public ProductResponse update(@PathVariable Long id, @RequestBody @Valid ProductRequest request) {
        return service.update(id, request);
    }

    // ADMIN
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    @PatchMapping("/{id}/stock/decrease")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void decreaseStock(@PathVariable Long id, @RequestBody @Valid UpdateStockRequest request) {
        service.decreaseStock(id, request.quantity());
    }

    @PatchMapping("/{id}/stock/increase")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void increaseStock(@PathVariable Long id, @RequestBody @Valid UpdateStockRequest request) {
        service.increaseStock(id, request.quantity());
    }
}