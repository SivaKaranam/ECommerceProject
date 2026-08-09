package com.sivakaranam.ecommerce.product.repository;

import com.sivakaranam.ecommerce.product.model.Category;
import com.sivakaranam.ecommerce.product.model.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A slice test against a real (H2) database, unlike ProductServiceTest's pure
 * Mockito tests. This exercises the actual @EntityGraph annotation on
 * ProductRepository.findAll(Pageable), not just the mocked contract.
 */
@DataJpaTest
@ActiveProfiles("test")
class ProductRepositoryDataJpaTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private jakarta.persistence.EntityManager entityManager;

    @Test
    void findAll_loadsCategoryEagerlyViaEntityGraph_evenAfterPersistenceContextIsCleared() {
        // data.sql already seeds "Electronics"/"Books" for other tests sharing this
        // database, so a distinct name avoids a unique-constraint collision.
        Category category = new Category();
        category.setName("DataJpaTest Category");
        categoryRepository.save(category);

        Product product = new Product();
        product.setName("Clean Code");
        product.setPrice(new BigDecimal("899.00"));
        product.setStockQuantity(20);
        product.setCategory(category);
        productRepository.save(product);

        // Force a fresh load from the database, past the persistence context cache.
        entityManager.flush();
        entityManager.clear();

        Page<Product> page = productRepository.findAll(PageRequest.of(0, 10));

        assertThat(page.getContent())
                .filteredOn(p -> p.getName().equals("Clean Code"))
                .singleElement()
                .satisfies(p -> assertThat(p.getCategory().getName()).isEqualTo("DataJpaTest Category"));
    }

    @Test
    void findById_loadsCategoryEagerlyViaEntityGraph_evenAfterPersistenceContextIsCleared() {
        // findById has its own @EntityGraph override, separate from findAll's,
        // so it needs its own coverage rather than relying on the test above.
        Category category = new Category();
        category.setName("DataJpaTest Category 2");
        categoryRepository.save(category);

        Product product = new Product();
        product.setName("Designing Data-Intensive Applications");
        product.setPrice(new BigDecimal("1299.00"));
        product.setStockQuantity(15);
        product.setCategory(category);
        Product saved = productRepository.save(product);

        entityManager.flush();
        entityManager.clear();

        Product found = productRepository.findById(saved.getId()).orElseThrow();

        assertThat(found.getCategory().getName()).isEqualTo("DataJpaTest Category 2");
    }
}
