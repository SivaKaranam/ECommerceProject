package com.sivakaranam.ecommerce.order.dto;

import com.sivakaranam.ecommerce.order.model.CartItem;

import java.math.BigDecimal;

public record CartItemResponse(
        Long id,
        Long productId,
        String productName,
        BigDecimal unitPrice,
        int quantity
) {
    public static CartItemResponse from(CartItem item) {
        return new CartItemResponse(item.getId(), item.getProductId(), item.getProductName(), item.getUnitPrice(), item.getQuantity());
    }
}
