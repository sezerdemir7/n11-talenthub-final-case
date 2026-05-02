package com.demir.ecommerce.productservice.service.impl;

import com.demir.ecommerce.commonlib.excepption.BusinessException;
import com.demir.ecommerce.commonlib.excepption.message.GeneralErrorMessage;
import com.demir.ecommerce.commonlib.security.SecurityUtils;
import com.demir.ecommerce.productservice.dto.product.*;
import com.demir.ecommerce.productservice.dto.productdetail.ProductDetailCreateRequest;
import com.demir.ecommerce.productservice.dto.productdetail.ProductDetailResponse;
import com.demir.ecommerce.productservice.dto.productdetail.ProductDetailUpdateRequest;
import com.demir.ecommerce.productservice.entity.Category;
import com.demir.ecommerce.productservice.entity.Product;
import com.demir.ecommerce.productservice.entity.ProductDetail;
import com.demir.ecommerce.productservice.exception.message.CategoryErrorMessage;
import com.demir.ecommerce.productservice.exception.message.ProductErrorMessage;
import com.demir.ecommerce.productservice.repository.CategoryRepository;
import com.demir.ecommerce.productservice.repository.ProductRepository;
import com.demir.ecommerce.productservice.service.ProductService;
import com.demir.ecommerce.productservice.service.StorageService;
import com.demir.ecommerce.productservice.specification.ProductSpecification;
import com.demir.ecommerce.productservice.util.SlugUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;

@Service
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final StorageService storageService;

    public ProductServiceImpl(
            ProductRepository productRepository,
            CategoryRepository categoryRepository,
            StorageService storageService
    ) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.storageService = storageService;
    }

    @Override
    public ProductResponse create(ProductCreateRequest request, MultipartFile image) {

        requireSellerOrAdmin();

        Category category = findCategoryById(request.categoryId());

        Long sellerId = SecurityUtils.getUserId();

        Product product = new Product();
        product.setSellerId(sellerId);
        product.setName(request.name());
        product.setSlug(generateUniqueSlug(SlugUtil.toSlug(request.name()), null));
        product.setPrice(request.price());
        product.setStock(request.stock());
        product.setActive(request.active() != null ? request.active() : true);
        product.setCategory(category);

        if (request.detail() != null) {
            ProductDetail detail = toProductDetail(request.detail());
            product.setDetail(detail);
            detail.setProduct(product);
        }

        Product savedProduct = productRepository.save(product);

        if (image != null && !image.isEmpty()) {
            String imageUrl = storageService.uploadProductImage(image, savedProduct.getId());
            savedProduct.setImageUrl(imageUrl);
            savedProduct = productRepository.save(savedProduct);
        }

        return toProductResponse(savedProduct);
    }

    @Override
    public ProductResponse update(Long id, ProductUpdateRequest request, MultipartFile image) {
        Product product = findProductById(id);

        if (!SecurityUtils.isOwnerOrAdmin(product.getSellerId())) {
            throw new BusinessException(GeneralErrorMessage.ACCESS_DENIED);
        }

        if (request.name() != null && !request.name().isBlank()) {
            product.setName(request.name());
            product.setSlug(generateUniqueSlug(SlugUtil.toSlug(request.name()), product.getId()));
        }

        if (request.price() != null) {
            product.setPrice(request.price());
        }

        if (request.stock() != null) {
            product.setStock(request.stock());
        }

        if (request.active() != null) {
            product.setActive(request.active());
            product.setSuspendedBySellerStatus(false);
        }

        if (request.categoryId() != null) {
            product.setCategory(findCategoryById(request.categoryId()));
        }

        if (image != null && !image.isEmpty()) {
            if (product.getImageUrl() != null && !product.getImageUrl().isBlank()) {
                storageService.deleteFile(product.getImageUrl());
            }
            product.setImageUrl(storageService.uploadProductImage(image, product.getId()));
        }

        if (request.detail() != null) {
            updateProductDetail(product, request.detail());
        }

        return toProductResponse(productRepository.save(product));
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getById(Long id) {
        return toProductResponse(findProductById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getBySlug(String slug) {
        return productRepository.findBySlug(slug)
                .map(this::toProductResponse)
                .orElseThrow(() -> new BusinessException(ProductErrorMessage.PRODUCT_NOT_FOUND));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductListResponse> search(ProductFilterRequest filter, Pageable pageable) {
        return productRepository.findAll(ProductSpecification.filter(filter), pageable)
                .map(this::toProductListResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductListResponse> getBySellerId(Long sellerId, Pageable pageable) {
        ProductFilterRequest filter = new ProductFilterRequest(
                null,
                null,
                sellerId,
                null,
                null,
                null,
                null,
                null
        );

        return productRepository.findAll(ProductSpecification.filter(filter), pageable)
                .map(this::toProductListResponse);
    }

    @Override
    public void delete(Long id) {
        Product product = findProductById(id);

        if (!SecurityUtils.isOwnerOrAdmin(product.getSellerId())) {
            throw new BusinessException(GeneralErrorMessage.ACCESS_DENIED);
        }

        if (product.getImageUrl() != null && !product.getImageUrl().isBlank()) {
            storageService.deleteFile(product.getImageUrl());
        }

        productRepository.delete(product);
    }

    @Override
    public List<ProductInternalResponse> getByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }

        return productRepository.findAllByIdIn(ids)
                .stream()
                .map(product -> new ProductInternalResponse(
                        product.getId(),
                        product.getName(),
                        product.getImageUrl(),
                        product.getPrice(),
                        product.getStock(),
                        product.getActive()
                ))
                .toList();
    }

    @Override
    @Transactional
    public void deactivateProductsBySellerId(Long sellerId) {
        List<Product> products = productRepository.findBySellerIdAndActiveTrue(sellerId);
        products.forEach(product -> {
            product.setActive(false);
            product.setSuspendedBySellerStatus(true);
        });
        productRepository.saveAll(products);
    }

    @Override
    @Transactional
    public void activateProductsBySellerId(Long sellerId) {
        List<Product> products = productRepository.findBySellerIdAndSuspendedBySellerStatusTrue(sellerId);
        products.forEach(product -> {
            product.setActive(true);
            product.setSuspendedBySellerStatus(false);
        });
        productRepository.saveAll(products);
    }


    private void requireSellerOrAdmin() {
        if (!SecurityUtils.hasRole("SELLER") && !SecurityUtils.isAdmin()) {
            throw new BusinessException(GeneralErrorMessage.ACCESS_DENIED);
        }
    }

    private Product findProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ProductErrorMessage.PRODUCT_NOT_FOUND));
    }

    private Category findCategoryById(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new BusinessException(CategoryErrorMessage.CATEGORY_NOT_FOUND));
    }

    private String generateUniqueSlug(String value, Long currentProductId) {
        String baseSlug = SlugUtil.toSlug(value);
        String slug = baseSlug;
        int counter = 2;

        while (isSlugUsedByAnotherProduct(slug, currentProductId)) {
            slug = baseSlug + "-" + counter++;
        }

        return slug;
    }

    private boolean isSlugUsedByAnotherProduct(String slug, Long currentProductId) {
        return productRepository.findBySlug(slug)
                .map(product -> !Objects.equals(product.getId(), currentProductId))
                .orElse(false);
    }

    private ProductDetail toProductDetail(ProductDetailCreateRequest request) {
        ProductDetail detail = new ProductDetail();
        detail.setShortDescription(request.shortDescription());
        detail.setLongDescription(request.longDescription());
        detail.setBrand(request.brand());
        detail.setModel(request.model());
        detail.setWarrantyPeriod(request.warrantyPeriod());
        detail.setSpecifications(request.specifications());
        return detail;
    }

    private void updateProductDetail(Product product, ProductDetailUpdateRequest request) {
        ProductDetail detail = product.getDetail();

        if (detail == null) {
            detail = new ProductDetail();
            detail.setProduct(product);
            product.setDetail(detail);
        }

        if (request.shortDescription() != null) detail.setShortDescription(request.shortDescription());
        if (request.longDescription() != null)  detail.setLongDescription(request.longDescription());
        if (request.brand() != null)             detail.setBrand(request.brand());
        if (request.model() != null)             detail.setModel(request.model());
        if (request.warrantyPeriod() != null)    detail.setWarrantyPeriod(request.warrantyPeriod());
        if (request.specifications() != null)    detail.setSpecifications(request.specifications());
    }

    private ProductResponse toProductResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getSellerId(),
                product.getName(),
                product.getSlug(),
                product.getPrice(),
                product.getStock(),
                product.getImageUrl(),
                product.getActive(),
                product.getCategory().getId(),
                product.getCategory().getName(),
                toProductDetailResponse(product.getDetail()),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }

    private ProductListResponse toProductListResponse(Product product) {
        return new ProductListResponse(
                product.getId(),
                product.getName(),
                product.getSlug(),
                product.getPrice(),
                product.getStock(),
                product.getImageUrl(),
                product.getActive(),
                product.getCategory().getId(),
                product.getCategory().getName()
        );
    }

    private ProductDetailResponse toProductDetailResponse(ProductDetail detail) {
        if (detail == null) return null;

        return new ProductDetailResponse(
                detail.getId(),
                detail.getShortDescription(),
                detail.getLongDescription(),
                detail.getBrand(),
                detail.getModel(),
                detail.getWarrantyPeriod(),
                detail.getSpecifications()
        );
    }
}
