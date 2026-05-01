package com.demir.ecommerce.productservice.service;

import com.demir.ecommerce.productservice.dto.category.request.CategoryCreateRequest;
import com.demir.ecommerce.productservice.dto.category.response.CategoryFilterResponse;
import com.demir.ecommerce.productservice.dto.category.response.CategoryResponse;
import com.demir.ecommerce.productservice.dto.category.response.CategoryTreeResponse;
import com.demir.ecommerce.productservice.dto.category.request.CategoryUpdateRequest;

import java.util.List;

public interface CategoryService {

    CategoryResponse create(CategoryCreateRequest request);

    CategoryResponse update(Long id, CategoryUpdateRequest request);

    CategoryResponse getById(Long id);

    CategoryResponse getBySlug(String slug);

    List<CategoryResponse> getAll();

    List<CategoryTreeResponse> getTree();

    List<CategoryFilterResponse> getFilters();

    void delete(Long id);
}