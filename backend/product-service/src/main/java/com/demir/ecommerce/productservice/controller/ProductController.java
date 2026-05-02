package com.demir.ecommerce.productservice.controller;

import com.demir.ecommerce.commonlib.dto.RestResponse;
import com.demir.ecommerce.productservice.dto.product.*;
import com.demir.ecommerce.productservice.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

@Tag(name = "Product", description = "Product management operations")
@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @Operation(summary = "Create product", description = "Creates a new product with optional image")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RestResponse<ProductResponse>> create(
            @Valid @RequestPart("request") ProductCreateRequest request,
            @RequestPart("image") MultipartFile image
    ) {
        ProductResponse response = productService.create(request, image);
        return ResponseEntity.ok(RestResponse.of(response, "Product created successfully"));
    }

    @Operation(summary = "Update product", description = "Updates product information with optional image")
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RestResponse<ProductResponse>> update(
            @PathVariable Long id,
            @Valid @RequestPart("request") ProductUpdateRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        ProductResponse response = productService.update(id, request, image);
        return ResponseEntity.ok(RestResponse.of(response, "Product updated successfully"));
    }

    @Operation(summary = "Get product by id", description = "Returns product by id")
    @GetMapping("/{id}")
    public ResponseEntity<RestResponse<ProductResponse>> getById(@PathVariable Long id) {
        ProductResponse response = productService.getById(id);
        return ResponseEntity.ok(RestResponse.of(response));
    }

    @Operation(summary = "Get product by slug", description = "Returns product by slug")
    @GetMapping("/slug/{slug}")
    public ResponseEntity<RestResponse<ProductResponse>> getBySlug(@PathVariable String slug) {
        ProductResponse response = productService.getBySlug(slug);
        return ResponseEntity.ok(RestResponse.of(response));
    }

    @Operation(summary = "Search products", description = "Returns paginated products with filters")
    @GetMapping
    public ResponseEntity<RestResponse<Page<ProductListResponse>>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long sellerId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Boolean active,
            @PageableDefault(size = 12, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        ProductFilterRequest filter = new ProductFilterRequest(
                keyword,
                categoryId,
                sellerId,
                minPrice,
                maxPrice,
                active
        );

        Page<ProductListResponse> response = productService.search(filter, pageable);
        return ResponseEntity.ok(RestResponse.of(response));
    }

    @Operation(summary = "Get products by seller", description = "Returns paginated products by seller id")
    @GetMapping("/seller/{sellerId}")
    public ResponseEntity<RestResponse<Page<ProductListResponse>>> getBySellerId(
            @PathVariable Long sellerId,
            @PageableDefault(size = 12, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<ProductListResponse> response = productService.getBySellerId(sellerId, pageable);
        return ResponseEntity.ok(RestResponse.of(response));
    }

    @Operation(summary = "Delete product", description = "Deletes product by id")
    @DeleteMapping("/{id}")
    public ResponseEntity<RestResponse<Void>> delete(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.ok(RestResponse.of(null, "Product deleted successfully"));
    }
}