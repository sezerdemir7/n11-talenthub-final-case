package com.demir.ecommerce.orderservice.repository;

import com.demir.ecommerce.orderservice.entity.Order;
import com.demir.ecommerce.orderservice.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @EntityGraph(attributePaths = "items")
    Optional<Order> findWithItemsById(Long id);

    @EntityGraph(attributePaths = "items")
    Page<Order> findAllByUserId(Long userId, Pageable pageable);

    Page<Order> findAllByUserIdAndStatusNot(Long userId, OrderStatus status, Pageable pageable);

    @EntityGraph(attributePaths = "items")
    List<Order> findAllByStatusAndCreatedAtBefore(OrderStatus status, LocalDateTime createdAt);

}