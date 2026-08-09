package com.sivakaranam.ecommerce.notification.kafka;

public record PaymentCompletedEvent(
        Long orderId,
        Long paymentId,
        Long userId,
        String status
) {
}
