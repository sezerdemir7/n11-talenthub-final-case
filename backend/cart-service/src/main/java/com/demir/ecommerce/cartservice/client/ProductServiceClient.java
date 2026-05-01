package com.demir.ecommerce.cartservice.client;

import com.demir.ecommerce.cartservice.dto.product.ProductInternalResponse;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "product-service",path = "/internal/products")
public interface ProductServiceClient {

    @GetMapping()
    List<ProductInternalResponse> getProductsByIds(@RequestParam List<Long> ids);
}