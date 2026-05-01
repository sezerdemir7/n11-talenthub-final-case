package com.demir.ecommerce.productservice.entity;

import com.demir.ecommerce.commonlib.entity.BaseEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "product_details")
public class ProductDetail extends BaseEntity {

    @Column(length = 500)
    private String shortDescription;

    @Column(columnDefinition = "TEXT")
    private String longDescription;

    private String brand;

    private String model;

    private String warrantyPeriod;

    @Column(columnDefinition = "TEXT")
    private String specifications;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false, unique = true)
    private Product product;

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public String getLongDescription() {
        return longDescription;
    }

    public void setLongDescription(String longDescription) {
        this.longDescription = longDescription;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getWarrantyPeriod() {
        return warrantyPeriod;
    }

    public void setWarrantyPeriod(String warrantyPeriod) {
        this.warrantyPeriod = warrantyPeriod;
    }

    public String getSpecifications() {
        return specifications;
    }

    public void setSpecifications(String specifications) {
        this.specifications = specifications;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }
}