package com.shopflow.order.client;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.shopflow.order.dto.response.ProductResponse;
import com.shopflow.order.exception.custom.InsufficientStockException;
import com.shopflow.order.exception.custom.OrderProductNotFoundException;
import com.shopflow.order.exception.custom.ProductServiceUnavailableException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClient;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductClientIntegrationTest {

    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    private ProductClient productClient;

    @BeforeEach
    void setUp() {
        RestClient restClient = RestClient.builder()
                .requestFactory(new HttpComponentsClientHttpRequestFactory())
                .build();

        productClient = new ProductClient(restClient);

        ReflectionTestUtils.setField(
                productClient,
                "productServiceUrl",
                wireMock.getRuntimeInfo().getHttpBaseUrl()
        );
    }
    @Test
    void shouldGetProductById() {
        wireMock.stubFor(get(urlEqualTo("/api/v1/products/10"))
                .willReturn(okJson("""
                        {
                          "id": 10,
                          "name": "Keyboard",
                          "description": "Mechanical keyboard",
                          "price": 50.0,
                          "stock": 20,
                          "active": true,
                          "createdAt": "2026-05-23T10:00:00",
                          "updatedAt": null
                        }
                        """)));

        ProductResponse response = productClient.getProductById(10L);

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.name()).isEqualTo("Keyboard");
        assertThat(response.price()).isEqualTo(50.0);
        assertThat(response.stock()).isEqualTo(20);

        wireMock.verify(getRequestedFor(urlEqualTo("/api/v1/products/10")));
    }

    @Test
    void shouldThrowOrderProductNotFoundWhenProductDoesNotExist() {
        wireMock.stubFor(get(urlEqualTo("/api/v1/products/999"))
                .willReturn(notFound()));

        assertThatThrownBy(() -> productClient.getProductById(999L))
                .isInstanceOf(OrderProductNotFoundException.class);

        wireMock.verify(getRequestedFor(urlEqualTo("/api/v1/products/999")));
    }

    @Test
    void shouldDecreaseStock() {
        wireMock.stubFor(patch(urlEqualTo("/api/v1/products/10/stock/decrease"))
                .withRequestBody(equalToJson("""
                        {
                          "quantity": 3
                        }
                        """))
                .willReturn(noContent()));

        productClient.decreaseStock(10L, 3);

        wireMock.verify(patchRequestedFor(
                urlEqualTo("/api/v1/products/10/stock/decrease")
        ).withRequestBody(equalToJson("""
                {
                  "quantity": 3
                }
                """)));
    }

    @Test
    void shouldThrowInsufficientStockWhenDecreaseStockReturnsConflict() {
        wireMock.stubFor(patch(urlEqualTo("/api/v1/products/10/stock/decrease"))
                .willReturn(status(409)));

        assertThatThrownBy(() -> productClient.decreaseStock(10L, 99))
                .isInstanceOf(InsufficientStockException.class);

        wireMock.verify(patchRequestedFor(
                urlEqualTo("/api/v1/products/10/stock/decrease")
        ));
    }

    @Test
    void shouldIncreaseStock() {
        wireMock.stubFor(patch(urlEqualTo("/api/v1/products/10/stock/increase"))
                .withRequestBody(equalToJson("""
                        {
                          "quantity": 5
                        }
                        """))
                .willReturn(noContent()));

        productClient.increaseStock(10L, 5);

        wireMock.verify(patchRequestedFor(
                urlEqualTo("/api/v1/products/10/stock/increase")
        ).withRequestBody(equalToJson("""
                {
                  "quantity": 5
                }
                """)));
    }

    @Test
    void shouldThrowProductServiceUnavailableWhenRemoteServiceFails() {
        wireMock.stubFor(get(urlEqualTo("/api/v1/products/10"))
                .willReturn(serverError()));

        assertThatThrownBy(() -> productClient.getProductById(10L))
                .isInstanceOf(ProductServiceUnavailableException.class);
    }
}