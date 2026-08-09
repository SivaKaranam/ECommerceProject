package com.sivakaranam.ecommerce.order.dto;

import com.sivakaranam.ecommerce.order.model.Order;
import com.sivakaranam.ecommerce.order.model.OrderStatus;

import java.math.BigDecimal;
import java.util.List;

public record OrderResponse(
        Long id,
        OrderStatus status,
        BigDecimal totalAmount,
        String paymentId,
        List<OrderItemResponse> items
) {
    public static OrderResponse from(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getPaymentId(),
                order.getItems().stream().map(OrderItemResponse::from).toList()
        );
    }
}
