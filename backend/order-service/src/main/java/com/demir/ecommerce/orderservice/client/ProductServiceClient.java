package com.demir.ecommerce.orderservice.client;




import com.demir.ecommerce.orderservice.dto.product.ProductInternalResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "product-service", path = "/internal/products")
public interface ProductServiceClient {

    @GetMapping
    List<ProductInternalResponse> getByIds(
            @RequestParam List<Long> ids
    );

    @PostMapping("/decrease-stock")
    void decreaseStock(
            @RequestParam Long productId,
            @RequestParam Integer quantity
    );
}
