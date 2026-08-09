package com.sivakaranam.ecommerce.product.service;

import com.sivakaranam.ecommerce.common.exception.BadRequestException;
import com.sivakaranam.ecommerce.common.exception.ResourceNotFoundException;
import com.sivakaranam.ecommerce.product.dto.CategoryRequest;
import com.sivakaranam.ecommerce.product.model.Category;
import com.sivakaranam.ecommerce.product.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public Category create(CategoryRequest request) {
        if (categoryRepository.existsByName(request.name())) {
            throw new BadRequestException("Category already exists: " + request.name());
        }
        Category category = new Category();
        category.setName(request.name());
        return categoryRepository.save(category);
    }

    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    public Category findById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No category with id " + id));
    }
}
