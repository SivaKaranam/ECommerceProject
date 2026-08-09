package com.sivakaranam.ecommerce.payment.gateway;

import java.math.BigDecimal;

public interface PaymentGateway {

    GatewayPaymentLink createPaymentLink(Long orderId, BigDecimal amount);

    boolean verifyWebhookSignature(String rawPayload, String signatureHeader);

    /** "paid", "cancelled", "expired", or "created" (still pending). */
    String fetchPaymentLinkStatus(String gatewayLinkId);
}
