package com.sivakaranam.ecommerce.product.dto;

import jakarta.validation.constraints.NotBlank;

public record CategoryRequest(
        @NotBlank(message = "name is required")
        String name
) {
}
