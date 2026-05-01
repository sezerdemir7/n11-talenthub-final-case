package com.demir.ecommerce.cartservice.entity;



import com.demir.ecommerce.commonlib.entity.BaseEntity;
import jakarta.persistence.*;


import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "carts")
public class Cart extends BaseEntity {


    // User microservice olduğu için sadece ID tutuyoruz
    @Column(nullable = false, unique = true)
    private Long userId;

    @OneToMany(
            mappedBy = "cart",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<CartItem> items = new ArrayList<>();

    public Cart() {
    }

    public Cart(Long userId, List<CartItem> items) {
        this.userId = userId;
        this.items = items;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
    }

}
