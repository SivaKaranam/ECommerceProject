package com.sivakaranam.ecommerce.order.dto;

import com.sivakaranam.ecommerce.order.model.OrderItem;

import java.math.BigDecimal;

public record OrderItemResponse(
        Long productId,
        String productName,
        BigDecimal unitPrice,
        int quantity
) {
    public static OrderItemResponse from(OrderItem item) {
        return new OrderItemResponse(item.getProductId(), item.getProductName(), item.getUnitPrice(), item.getQuantity());
    }
}
