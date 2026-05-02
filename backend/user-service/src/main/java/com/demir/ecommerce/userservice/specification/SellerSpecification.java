package com.demir.ecommerce.userservice.specification;

import com.demir.ecommerce.userservice.dto.seller.request.SellerFilterRequest;
import com.demir.ecommerce.userservice.entity.SellerProfile;
import org.springframework.data.jpa.domain.Specification;

public class SellerSpecification {

    private SellerSpecification() {}

    public static Specification<SellerProfile> filter(SellerFilterRequest filter) {
        return Specification
                .where(hasStoreName(filter.storeName()))
                .and(hasStatus(filter.status()))
                .and(hasVerified(filter.verified()));
    }

    private static Specification<SellerProfile> hasStoreName(String storeName) {
        return (root, query, cb) -> {
            if (storeName == null || storeName.isBlank()) return null;
            return cb.like(cb.lower(root.get("storeName")), "%" + storeName.toLowerCase() + "%");
        };
    }

    private static Specification<SellerProfile> hasStatus(com.demir.ecommerce.userservice.entity.SellerStatus status) {
        return (root, query, cb) -> {
            if (status == null) return null;
            return cb.equal(root.get("status"), status);
        };
    }

    private static Specification<SellerProfile> hasVerified(Boolean verified) {
        return (root, query, cb) -> {
            if (verified == null) return null;
            return cb.equal(root.get("isVerified"), verified);
        };
    }
}