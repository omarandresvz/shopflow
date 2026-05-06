package com.shopflow.product.repository;

import com.shopflow.product.entity.Product;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByActiveTrue();

    Optional<Product> findByIdAndActiveTrue(Long id);
    
}