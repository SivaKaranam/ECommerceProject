package com.sivakaranam.ecommerce.payment.dto;

import com.sivakaranam.ecommerce.payment.model.Payment;

public record PaymentResponse(
        Long paymentId,
        String paymentLink,
        String status
) {
    public static PaymentResponse from(Payment payment) {
        return new PaymentResponse(payment.getId(), payment.getGatewayLinkUrl(), payment.getStatus().name());
    }
}
