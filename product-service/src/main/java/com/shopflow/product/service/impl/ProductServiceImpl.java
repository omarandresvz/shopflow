package com.shopflow.product.service.impl;

import com.shopflow.product.dto.request.ProductRequest;
import com.shopflow.product.dto.response.ProductResponse;
import com.shopflow.product.entity.Product;
import com.shopflow.product.exception.custom.InsufficientStockException;
import com.shopflow.product.exception.custom.ProductNotFoundException;
import com.shopflow.product.repository.ProductRepository;
import com.shopflow.product.service.ProductService;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository repository;

    @Override
    @Transactional
    public ProductResponse create(ProductRequest request) {
        Product product = Product.builder()
                .name(request.name())
                .description(request.description())
                .price(request.price())
                .stock(request.stock())
                .active(true)
                .build();

        Product saved = repository.save(product);

        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> findAll() {
        return repository.findByActiveTrue()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse findById(Long id) {
        Product product = repository.findByIdAndActiveTrue(id)
                .orElseThrow(ProductNotFoundException::new);

        return mapToResponse(product);
    }

    @Override
    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = repository.findByIdAndActiveTrue(id)
                .orElseThrow(ProductNotFoundException::new);

        product.setName(request.name());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setStock(request.stock());

        Product updated = repository.save(product);

        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Product product = repository.findByIdAndActiveTrue(id)
                .orElseThrow(ProductNotFoundException::new);

        product.setActive(false);
        repository.save(product);
    }

    private ProductResponse mapToResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStock(),
                product.getActive(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }

    @Override
    @Transactional
    public void decreaseStock(Long productId, Integer quantity) {

        Product product = repository.findByIdAndActiveTrue(productId)
                .orElseThrow(ProductNotFoundException::new);

        if (product.getStock() < quantity) {
            throw new InsufficientStockException();
        }

        product.setStock(product.getStock() - quantity);

        repository.save(product);
    }
}