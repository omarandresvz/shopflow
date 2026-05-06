package com.shopflow.product.controller;

import com.shopflow.product.dto.ProductRequest;
import com.shopflow.product.dto.ProductResponse;
import com.shopflow.product.service.ProductService;
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
    public ProductResponse create(@RequestBody ProductRequest request) {
        return service.create(request);
    }

    // ADMIN
    @PutMapping("/{id}")
    public ProductResponse update(
            @PathVariable Long id,
            @RequestBody ProductRequest request
    ) {
        return service.update(id, request);
    }

    // ADMIN
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}