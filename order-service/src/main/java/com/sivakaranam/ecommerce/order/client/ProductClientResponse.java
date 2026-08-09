package com.sivakaranam.ecommerce.order.client;

import java.math.BigDecimal;

public record ProductClientResponse(
        Long id,
        String name,
        String description,
        BigDecimal price,
        int stockQuantity,
        Long categoryId,
        String categoryName
) {
}
