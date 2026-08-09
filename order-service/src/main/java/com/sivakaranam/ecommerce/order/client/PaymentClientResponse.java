package com.sivakaranam.ecommerce.order.client;

public record PaymentClientResponse(
        Long paymentId,
        String paymentLink,
        String status
) {
}
