package com.demir.ecommerce.productservice.specification;

import com.demir.ecommerce.productservice.dto.product.ProductFilterRequest;
import com.demir.ecommerce.productservice.entity.Product;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public final class ProductSpecification {

    private ProductSpecification() {
    }

    public static Specification<Product> filter(ProductFilterRequest filter) {
        Specification<Product> spec = alwaysTrue();

        spec = spec.and(hasKeyword(filter.keyword()));
        spec = spec.and(hasCategory(filter.categoryId()));
        spec = spec.and(hasSeller(filter.sellerId()));
        spec = spec.and(hasMinPrice(filter.minPrice()));
        spec = spec.and(hasMaxPrice(filter.maxPrice()));
        spec = spec.and(hasActive(filter.active()));

        return spec;
    }

    private static Specification<Product> alwaysTrue() {
        return (root, query, cb) -> cb.conjunction();
    }

    private static Specification<Product> hasKeyword(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) {
                return cb.conjunction();
            }

            String likeKeyword = "%" + keyword.toLowerCase() + "%";

            return cb.or(
                    cb.like(cb.lower(root.get("name")), likeKeyword),
                    cb.like(cb.lower(root.get("slug")), likeKeyword)
            );
        };
    }

    private static Specification<Product> hasCategory(Long categoryId) {
        return (root, query, cb) -> {
            if (categoryId == null) {
                return cb.conjunction();
            }

            return cb.equal(root.get("category").get("id"), categoryId);
        };
    }

    private static Specification<Product> hasSeller(Long sellerId) {
        return (root, query, cb) -> {
            if (sellerId == null) {
                return cb.conjunction();
            }

            return cb.equal(root.get("sellerId"), sellerId);
        };
    }

    private static Specification<Product> hasMinPrice(BigDecimal minPrice) {
        return (root, query, cb) -> {
            if (minPrice == null) {
                return cb.conjunction();
            }

            return cb.greaterThanOrEqualTo(root.get("price"), minPrice);
        };
    }

    private static Specification<Product> hasMaxPrice(BigDecimal maxPrice) {
        return (root, query, cb) -> {
            if (maxPrice == null) {
                return cb.conjunction();
            }

            return cb.lessThanOrEqualTo(root.get("price"), maxPrice);
        };
    }

    private static Specification<Product> hasActive(Boolean active) {
        return (root, query, cb) -> {
            if (active == null) {
                return cb.isTrue(root.get("active"));
            }

            return cb.equal(root.get("active"), active);
        };
    }
}
