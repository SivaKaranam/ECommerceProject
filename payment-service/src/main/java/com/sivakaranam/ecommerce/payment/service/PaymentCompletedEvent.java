package com.sivakaranam.ecommerce.payment.service;

public record PaymentCompletedEvent(
        Long orderId,
        Long paymentId,
        Long userId,
        String status // "SUCCESS" or "FAILED"
) {
}
