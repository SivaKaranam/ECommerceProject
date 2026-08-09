package com.sivakaranam.ecommerce.product.repository;

import com.sivakaranam.ecommerce.product.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    // @EntityGraph loads category eagerly for queries that need to render it,
    // overriding the LAZY fetch declared on the entity mapping itself.
    @EntityGraph(attributePaths = "category")
    Page<Product> findAll(Pageable pageable);

    @EntityGraph(attributePaths = "category")
    Page<Product> findByCategoryId(Long categoryId, Pageable pageable);

    @EntityGraph(attributePaths = "category")
    Optional<Product> findById(Long id);
}
