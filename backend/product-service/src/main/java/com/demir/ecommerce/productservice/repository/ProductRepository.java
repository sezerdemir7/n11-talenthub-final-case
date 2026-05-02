package com.demir.ecommerce.productservice.repository;

import com.demir.ecommerce.productservice.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    Optional<Product> findBySlug(String slug);
    List<Product> findAllByIdIn(List<Long> ids);
    List<Product> findBySellerIdAndActiveTrue(Long sellerId);
    List<Product> findBySellerIdAndSuspendedBySellerStatusTrue(Long sellerId);
}
