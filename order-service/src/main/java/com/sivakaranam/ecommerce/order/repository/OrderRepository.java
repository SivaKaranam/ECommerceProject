package com.sivakaranam.ecommerce.order.repository;

import com.sivakaranam.ecommerce.order.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @EntityGraph(attributePaths = "items")
    Page<Order> findByUserId(Long userId, Pageable pageable);

    @EntityGraph(attributePaths = "items")
    Optional<Order> findByIdAndUserId(Long id, Long userId);

    Optional<Order> findByPaymentId(String paymentId);
}
