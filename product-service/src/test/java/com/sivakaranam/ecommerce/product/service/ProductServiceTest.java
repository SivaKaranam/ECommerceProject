package com.sivakaranam.ecommerce.product.service;

import com.sivakaranam.ecommerce.product.dto.ProductRequest;
import com.sivakaranam.ecommerce.product.model.Category;
import com.sivakaranam.ecommerce.product.model.Product;
import com.sivakaranam.ecommerce.product.repository.ProductRepository;
import com.sivakaranam.ecommerce.product.search.ProductSearchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryService categoryService;

    @Mock
    private ProductSearchRepository productSearchRepository;

    private ProductService productService;

    private Category electronics;

    @BeforeEach
    void setUp() {
        productService = new ProductService(productRepository, categoryService, productSearchRepository);

        electronics = new Category();
        electronics.setId(1L);
        electronics.setName("Electronics");
    }

    @Test
    void create_savesProductWithResolvedCategory_andIndexesItInSearch() {
        when(categoryService.findById(1L)).thenReturn(electronics);
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product product = invocation.getArgument(0);
            product.setId(42L);
            return product;
        });

        ProductRequest request = new ProductRequest("Wireless Mouse", "Ergonomic", new BigDecimal("799.00"), 150, 1L);
        Product created = productService.create(request);

        assertThat(created.getId()).isEqualTo(42L);
        assertThat(created.getCategory()).isEqualTo(electronics);

        ArgumentCaptor<com.sivakaranam.ecommerce.product.search.ProductDocument> documentCaptor =
                ArgumentCaptor.forClass(com.sivakaranam.ecommerce.product.search.ProductDocument.class);
        verify(productSearchRepository).save(documentCaptor.capture());
        assertThat(documentCaptor.getValue().getName()).isEqualTo("Wireless Mouse");
    }

    @Test
    void create_stillSucceeds_whenElasticsearchIndexingFails() {
        when(categoryService.findById(1L)).thenReturn(electronics);
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product product = invocation.getArgument(0);
            product.setId(7L);
            return product;
        });
        // Simulates Elasticsearch being unreachable, a fake failure exercising the
        // catch-and-log path in ProductService rather than mocking a happy response.
        when(productSearchRepository.save(any())).thenThrow(new RuntimeException("ES unreachable"));

        ProductRequest request = new ProductRequest("Keyboard", null, new BigDecimal("1000.00"), 10, 1L);

        Product created = productService.create(request);

        assertThat(created.getId()).isEqualTo(7L);
    }
}
