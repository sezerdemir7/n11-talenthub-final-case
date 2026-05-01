package com.demir.ecommerce.userservice.entity;

import com.demir.ecommerce.commonlib.entity.BaseEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "seller_profiles")
public class SellerProfile extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false, length = 150)
    private String storeName;

    @Column(nullable = false, length = 150)
    private String companyName;

    @Column(nullable = false, unique = true, length = 50)
    private String taxNumber;

    @Column(length = 250)
    private String storeDescription;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SellerStatus status;

    @Column(nullable = false)
    private Boolean isVerified;

    public SellerProfile() {}

    public SellerProfile(User user, String storeName, String companyName,
                         String taxNumber, String storeDescription,
                         SellerStatus status, Boolean isVerified) {
        this.user = user;
        this.storeName = storeName;
        this.companyName = companyName;
        this.taxNumber = taxNumber;
        this.storeDescription = storeDescription;
        this.status = status;
        this.isVerified = isVerified;
    }

    // Lifecycle
    @PrePersist
    public void prePersist() {
        if (this.status == null) {
            this.status = SellerStatus.PENDING;
        }
        if (this.isVerified == null) {
            this.isVerified = false;
        }
    }



    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getTaxNumber() {
        return taxNumber;
    }

    public void setTaxNumber(String taxNumber) {
        this.taxNumber = taxNumber;
    }

    public String getStoreDescription() {
        return storeDescription;
    }

    public void setStoreDescription(String storeDescription) {
        this.storeDescription = storeDescription;
    }

    public SellerStatus getStatus() {
        return status;
    }

    public void setStatus(SellerStatus status) {
        this.status = status;
    }

    public Boolean getIsVerified() {
        return isVerified;
    }

    public void setIsVerified(Boolean verified) {
        isVerified = verified;
    }
}