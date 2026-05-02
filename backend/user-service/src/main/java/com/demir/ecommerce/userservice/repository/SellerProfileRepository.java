package com.demir.ecommerce.userservice.repository;

import com.demir.ecommerce.userservice.entity.SellerProfile;
import com.demir.ecommerce.userservice.entity.SellerStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SellerProfileRepository extends JpaRepository<SellerProfile, Long> , JpaSpecificationExecutor<SellerProfile> {

    Optional<SellerProfile> findByUserId(Long userId);
    @Query("SELECT sp FROM SellerProfile sp JOIN FETCH sp.user")
    Page<SellerProfile> findAllWithUser(Pageable pageable);

    boolean existsByUserId(Long userId);

    boolean existsByTaxNumber(String taxNumber);

    List<SellerProfile> findByStatus(SellerStatus status);
}
