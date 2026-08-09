package com.sivakaranam.ecommerce.order.client;

import java.math.BigDecimal;

public record CreatePaymentRequest(
        Long orderId,
        Long userId,
        BigDecimal amount
) {
}
