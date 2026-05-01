package com.demir.ecommerce.productservice.controller;

import com.demir.ecommerce.commonlib.dto.RestResponse;
import com.demir.ecommerce.productservice.dto.category.request.CategoryCreateRequest;
import com.demir.ecommerce.productservice.dto.category.request.CategoryUpdateRequest;
import com.demir.ecommerce.productservice.dto.category.response.CategoryFilterResponse;
import com.demir.ecommerce.productservice.dto.category.response.CategoryResponse;
import com.demir.ecommerce.productservice.dto.category.response.CategoryTreeResponse;
import com.demir.ecommerce.productservice.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Category", description = "Category management operations")
@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @Operation(summary = "Create category", description = "Creates a new category")
    @PostMapping
    public ResponseEntity<RestResponse<CategoryResponse>> create(
            @Valid @RequestBody CategoryCreateRequest request
    ) {
        CategoryResponse response = categoryService.create(request);
        return ResponseEntity.ok(RestResponse.of(response, "Category created successfully"));
    }

    @Operation(summary = "Update category", description = "Updates category information")
    @PutMapping("/{id}")
    public ResponseEntity<RestResponse<CategoryResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody CategoryUpdateRequest request
    ) {
        CategoryResponse response = categoryService.update(id, request);
        return ResponseEntity.ok(RestResponse.of(response, "Category updated successfully"));
    }

    @Operation(summary = "Get category by id", description = "Returns category by id")
    @GetMapping("/{id}")
    public ResponseEntity<RestResponse<CategoryResponse>> getById(@PathVariable Long id) {
        CategoryResponse response = categoryService.getById(id);
        return ResponseEntity.ok(RestResponse.of(response));
    }

    @Operation(summary = "Get category by slug", description = "Returns category by slug")
    @GetMapping("/slug/{slug}")
    public ResponseEntity<RestResponse<CategoryResponse>> getBySlug(@PathVariable String slug) {
        CategoryResponse response = categoryService.getBySlug(slug);
        return ResponseEntity.ok(RestResponse.of(response));
    }

    @Operation(summary = "Get all categories", description = "Returns all categories")
    @GetMapping
    public ResponseEntity<RestResponse<List<CategoryResponse>>> getAll() {
        List<CategoryResponse> response = categoryService.getAll();
        return ResponseEntity.ok(RestResponse.of(response));
    }

    @Operation(summary = "Get category tree", description = "Returns hierarchical category tree")
    @GetMapping("/tree")
    public ResponseEntity<RestResponse<List<CategoryTreeResponse>>> getTree() {
        List<CategoryTreeResponse> response = categoryService.getTree();
        return ResponseEntity.ok(RestResponse.of(response));
    }

    @Operation(summary = "Get category filters", description = "Returns categories for filtering")
    @GetMapping("/filters")
    public ResponseEntity<RestResponse<List<CategoryFilterResponse>>> getFilters() {
        List<CategoryFilterResponse> response = categoryService.getFilters();
        return ResponseEntity.ok(RestResponse.of(response));
    }

    @Operation(summary = "Delete category", description = "Deletes category by id")
    @DeleteMapping("/{id}")
    public ResponseEntity<RestResponse<Void>> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return ResponseEntity.ok(RestResponse.of(null, "Category deleted successfully"));
    }
}