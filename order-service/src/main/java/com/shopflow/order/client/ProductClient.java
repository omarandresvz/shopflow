package com.shopflow.order.client;

import com.shopflow.order.dto.ProductResponse;
import com.shopflow.order.dto.UpdateStockRequest;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class ProductClient {

    private final RestClient restClient;

    @Value("${services.product.url}")
    private String productServiceUrl;

    public ProductResponse getProductById(Long productId) {
        return restClient.get()
                .uri(productServiceUrl + "/api/v1/products/{id}", productId)
                .retrieve()
                .body(ProductResponse.class);
    }

    public void decreaseStock(Long productId, Integer quantity) {
        restClient.patch()
                .uri(productServiceUrl + "/api/v1/products/{id}/stock/decrease", productId)
                .body(new UpdateStockRequest(quantity))
                .retrieve()
                .toBodilessEntity();
    }
}