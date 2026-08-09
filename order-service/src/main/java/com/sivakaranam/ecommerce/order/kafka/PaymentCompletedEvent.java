package com.sivakaranam.ecommerce.order.kafka;

public record PaymentCompletedEvent(
        Long orderId,
        Long paymentId,
        Long userId,
        String status // "SUCCESS" or "FAILED"
) {
}
