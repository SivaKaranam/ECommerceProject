package com.sivakaranam.ecommerce.product.dto;

import com.sivakaranam.ecommerce.product.model.Category;

public record CategoryResponse(Long id, String name) {

    public static CategoryResponse from(Category category) {
        return new CategoryResponse(category.getId(), category.getName());
    }
}
