package com.shopflow.order.client;

import com.shopflow.order.dto.request.UpdateStockRequest;
import com.shopflow.order.dto.response.ProductResponse;
import com.shopflow.order.exception.custom.InsufficientStockException;
import com.shopflow.order.exception.custom.InvalidOrderStatusTransitionException;
import com.shopflow.order.exception.custom.OrderProductNotFoundException;
import com.shopflow.order.exception.custom.ProductServiceUnavailableException;
import com.shopflow.shared.exception.BusinessException;
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

        try {
            return restClient.get()
                    .uri(productServiceUrl + "/api/v1/products/{id}", productId)
                    .retrieve()
                    .onStatus(
                            status -> status.value() == 404,
                            (request, response) -> {
                                throw new OrderProductNotFoundException();
                            }
                    )
                    .body(ProductResponse.class);

        } catch (BusinessException ex) {
            throw ex;

        } catch (Exception ex) {
            throw new ProductServiceUnavailableException(ex);
        }
    }

    public void decreaseStock(Long productId, Integer quantity) {

        try {
            restClient.patch()
                    .uri(productServiceUrl + "/api/v1/products/{id}/stock/decrease", productId)
                    .body(new UpdateStockRequest(quantity))
                    .retrieve()
                    .onStatus(
                            status -> status.value() == 409,
                            (request, response) -> {
                                throw new InsufficientStockException();
                            }
                    )
                    .onStatus(
                            status -> status.value() == 404,
                            (request, response) -> {
                                throw new OrderProductNotFoundException();
                            }
                    )
                    .onStatus(
                            status -> status.value() == 400,
                            (request, response) -> {
                                throw new InvalidOrderStatusTransitionException();
                            }
                    )
                    .toBodilessEntity();

        } catch (BusinessException ex) {
            throw ex;

        } catch (Exception ex) {
            throw new ProductServiceUnavailableException(ex);
        }
    }

    public void increaseStock(Long productId, Integer quantity) {

        try {
            restClient.patch()
                    .uri(productServiceUrl + "/api/v1/products/{id}/stock/increase", productId)
                    .body(new UpdateStockRequest(quantity))
                    .retrieve()
                    .onStatus(
                            status -> status.value() == 404,
                            (request, response) -> {
                                throw new OrderProductNotFoundException();
                            }
                    )
                    .toBodilessEntity();

        } catch (BusinessException ex) {
            throw ex;

        } catch (Exception ex) {
            throw new ProductServiceUnavailableException(ex);
        }
    }
}