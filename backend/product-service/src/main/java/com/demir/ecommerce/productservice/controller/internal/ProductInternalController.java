package com.demir.ecommerce.productservice.controller.internal;

import com.demir.ecommerce.productservice.dto.product.ProductInternalResponse;

import com.demir.ecommerce.productservice.service.ProductService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/internal/products")
public class ProductInternalController {

    private final ProductService productService;

    public ProductInternalController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public List<ProductInternalResponse> getByIds(@RequestParam List<Long> ids) {
        return productService.getByIds(ids);
    }
}
