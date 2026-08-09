package com.sivakaranam.ecommerce.payment.service;

import com.sivakaranam.ecommerce.payment.gateway.GatewayPaymentLink;
import com.sivakaranam.ecommerce.payment.gateway.PaymentGateway;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * A fake test double: a real, working (if simplified) implementation, unlike
 * a Mockito mock which only returns canned answers for the exact calls you
 * stub. Useful here because reconciliation exercises a sequence of gateway
 * calls (create, then fetch status) where a fake's actual in-memory state
 * lets the test set up scenarios without wiring up every intermediate stub.
 */
public class FakePaymentGateway implements PaymentGateway {

    private final Map<String, String> linkStatusesById = new HashMap<>();
    private int linkCounter = 0;

    @Override
    public GatewayPaymentLink createPaymentLink(Long orderId, BigDecimal amount) {
        String id = "plink_fake_" + (++linkCounter);
        linkStatusesById.put(id, "created");
        return new GatewayPaymentLink(id, "https://fake-gateway.test/" + id);
    }

    @Override
    public boolean verifyWebhookSignature(String rawPayload, String signatureHeader) {
        return "valid-signature".equals(signatureHeader);
    }

    @Override
    public String fetchPaymentLinkStatus(String gatewayLinkId) {
        return linkStatusesById.getOrDefault(gatewayLinkId, "unknown");
    }

    public void markPaid(String gatewayLinkId) {
        linkStatusesById.put(gatewayLinkId, "paid");
    }
}
