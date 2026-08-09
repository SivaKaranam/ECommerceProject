package com.sivakaranam.ecommerce.product.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sivakaranam.ecommerce.common.exception.GlobalExceptionHandler;
import com.sivakaranam.ecommerce.common.exception.ResourceNotFoundException;
import com.sivakaranam.ecommerce.product.model.Category;
import com.sivakaranam.ecommerce.product.model.Product;
import com.sivakaranam.ecommerce.product.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * A slice test: only the web layer (this controller + Spring MVC infra) is
 * loaded, not the full application context. ProductService is mocked, so
 * this verifies request mapping, validation, and JSON shape, not business
 * logic, which is already covered by ProductServiceTest's plain Mockito tests.
 * Security filters are disabled here since GlobalExceptionHandler/routing is
 * what's under test, not auth.
 */
@WebMvcTest(ProductController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class ProductControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductService productService;

    @Test
    void listProducts_returnsPagedJson() throws Exception {
        Category category = new Category();
        category.setId(1L);
        category.setName("Electronics");

        Product product = new Product();
        product.setId(10L);
        product.setName("Wireless Mouse");
        product.setPrice(new BigDecimal("799.00"));
        product.setStockQuantity(50);
        product.setCategory(category);

        when(productService.list(any())).thenReturn(new PageImpl<>(List.of(product), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Wireless Mouse"))
                .andExpect(jsonPath("$.content[0].categoryName").value("Electronics"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void getProduct_whenNotFound_returns404WithErrorBody() throws Exception {
        when(productService.findById(999L)).thenThrow(new ResourceNotFoundException("No product with id 999"));

        mockMvc.perform(get("/api/products/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("No product with id 999"));
    }
}
