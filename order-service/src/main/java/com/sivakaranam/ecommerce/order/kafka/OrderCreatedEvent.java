package com.sivakaranam.ecommerce.order.kafka;

import java.math.BigDecimal;

public record OrderCreatedEvent(
        Long orderId,
        Long userId,
        String userEmail,
        BigDecimal totalAmount
) {
}
