package com.sivakaranam.ecommerce.product.controller;

import com.sivakaranam.ecommerce.product.dto.CategoryRequest;
import com.sivakaranam.ecommerce.product.dto.CategoryResponse;
import com.sivakaranam.ecommerce.product.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public List<CategoryResponse> list() {
        return categoryService.findAll().stream().map(CategoryResponse::from).toList();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryResponse> create(@Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.ok(CategoryResponse.from(categoryService.create(request)));
    }
}
