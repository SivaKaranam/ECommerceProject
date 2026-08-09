package com.sivakaranam.ecommerce.order.dto;

import com.sivakaranam.ecommerce.order.model.Cart;

import java.math.BigDecimal;
import java.util.List;

public record CartResponse(
        Long id,
        List<CartItemResponse> items,
        BigDecimal total
) {
    public static CartResponse from(Cart cart) {
        List<CartItemResponse> items = cart.getItems().stream().map(CartItemResponse::from).toList();
        BigDecimal total = items.stream()
                .map(item -> item.unitPrice().multiply(BigDecimal.valueOf(item.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new CartResponse(cart.getId(), items, total);
    }
}
