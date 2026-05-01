package com.demir.ecommerce.productservice.service;

import com.demir.ecommerce.productservice.dto.product.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProductService {

    ProductResponse create(Long sellerId, ProductCreateRequest request, MultipartFile image);

    ProductResponse update(Long sellerId, Long id, ProductUpdateRequest request, MultipartFile image);

    ProductResponse getById(Long id);

    ProductResponse getBySlug(String slug);

    Page<ProductListResponse> search(ProductFilterRequest filter, Pageable pageable);

    Page<ProductListResponse> getBySellerId(Long sellerId, Pageable pageable);

    void delete(Long sellerId, Long id);

    List<ProductInternalResponse> getByIds(List<Long> ids);

    void deactivateProductsBySellerId(Long sellerId);

}