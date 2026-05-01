package com.demir.ecommerce.userservice.repository;

import com.demir.ecommerce.userservice.entity.SellerProfile;
import com.demir.ecommerce.userservice.entity.SellerStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SellerProfileRepository extends JpaRepository<SellerProfile, Long> {

    Optional<SellerProfile> findByUserId(Long userId);

    boolean existsByUserId(Long userId);

    boolean existsByTaxNumber(String taxNumber);

    List<SellerProfile> findByStatus(SellerStatus status);
}
