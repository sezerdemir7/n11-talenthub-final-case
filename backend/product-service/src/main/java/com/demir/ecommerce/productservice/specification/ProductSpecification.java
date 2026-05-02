package com.demir.ecommerce.productservice.specification;

import com.demir.ecommerce.productservice.dto.product.ProductFilterRequest;
import com.demir.ecommerce.productservice.dto.product.SortOption;
import com.demir.ecommerce.productservice.entity.Product;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public final class ProductSpecification {

    private ProductSpecification() {}

    public static Specification<Product> filter(ProductFilterRequest filter) {
        return Specification
                .where(hasKeyword(filter.keyword()))
                .and(hasCategory(filter.categoryId()))
                .and(hasSeller(filter.sellerId()))
                .and(hasMinPrice(filter.minPrice()))
                .and(hasMaxPrice(filter.maxPrice()))
                .and(hasActive(filter.active()))
                .and(hasBrand(filter.brand()))
                .and(withSort(filter.sortBy()));
    }

    private static Specification<Product> hasKeyword(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) return null;

            String like = "%" + keyword.toLowerCase() + "%";

            Join<Object, Object> detail = root.join("detail", JoinType.LEFT);

            return cb.or(
                    cb.like(cb.lower(root.get("name")), like),
                    cb.like(cb.lower(root.get("slug")), like),
                    cb.like(cb.lower(detail.get("brand")), like)
            );
        };
    }

    private static Specification<Product> hasCategory(Long categoryId) {
        return (root, query, cb) -> {
            if (categoryId == null) return null;

            return cb.or(
                    cb.equal(root.get("category").get("id"), categoryId),
                    cb.equal(root.get("category").get("parent").get("id"), categoryId)
            );
        };
    }

    private static Specification<Product> hasSeller(Long sellerId) {
        return (root, query, cb) -> {
            if (sellerId == null) return null;
            return cb.equal(root.get("sellerId"), sellerId);
        };
    }

    private static Specification<Product> hasMinPrice(BigDecimal minPrice) {
        return (root, query, cb) -> {
            if (minPrice == null) return null;
            return cb.greaterThanOrEqualTo(root.get("price"), minPrice);
        };
    }

    private static Specification<Product> hasMaxPrice(BigDecimal maxPrice) {
        return (root, query, cb) -> {
            if (maxPrice == null) return null;
            return cb.lessThanOrEqualTo(root.get("price"), maxPrice);
        };
    }

    private static Specification<Product> hasActive(Boolean active) {
        return (root, query, cb) -> {
            if (active == null) return cb.isTrue(root.get("active"));
            return cb.equal(root.get("active"), active);
        };
    }

    private static Specification<Product> hasBrand(String brand) {
        return (root, query, cb) -> {
            if (brand == null || brand.isBlank()) return null;

            Join<Object, Object> detail = root.join("detail", JoinType.LEFT);
            return cb.like(cb.lower(detail.get("brand")), "%" + brand.toLowerCase() + "%");
        };
    }

    private static Specification<Product> withSort(SortOption sortBy) {
        return (root, query, cb) -> {
            if (sortBy == null || query == null) return null;

            switch (sortBy) {
                case PRICE_ASC  -> query.orderBy(cb.asc(root.get("price")));
                case PRICE_DESC -> query.orderBy(cb.desc(root.get("price")));
                case NAME_ASC   -> query.orderBy(cb.asc(root.get("name")));
                case NEWEST     -> query.orderBy(cb.desc(root.get("createdAt")));
            }

            return null;
        };
    }
}