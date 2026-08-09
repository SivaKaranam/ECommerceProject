package com.sivakaranam.ecommerce.payment.gateway;

import com.razorpay.PaymentLink;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class RazorpayPaymentGateway implements PaymentGateway {

    private static final Logger log = LoggerFactory.getLogger(RazorpayPaymentGateway.class);

    private final RazorpayClient razorpayClient;
    private final String webhookSecret;

    public RazorpayPaymentGateway(
            @Value("${app.payment.razorpay.key-id}") String keyId,
            @Value("${app.payment.razorpay.key-secret}") String keySecret,
            @Value("${app.payment.razorpay.webhook-secret}") String webhookSecret
    ) {
        try {
            this.razorpayClient = new RazorpayClient(keyId, keySecret);
        } catch (RazorpayException e) {
            throw new IllegalStateException("Failed to initialize Razorpay client", e);
        }
        this.webhookSecret = webhookSecret;
    }

    @Override
    public GatewayPaymentLink createPaymentLink(Long orderId, BigDecimal amount) {
        try {
            JSONObject request = new JSONObject();
            // Razorpay amounts are in the smallest currency unit (paise for INR).
            request.put("amount", amount.multiply(BigDecimal.valueOf(100)).longValueExact());
            request.put("currency", "INR");
            request.put("accept_partial", false);
            request.put("description", "Order #" + orderId);
            request.put("reference_id", orderId.toString());

            PaymentLink paymentLink = razorpayClient.paymentLink.create(request);
            String id = paymentLink.get("id");
            String shortUrl = paymentLink.get("short_url");
            return new GatewayPaymentLink(id, shortUrl);
        } catch (RazorpayException e) {
            log.error("Failed to create Razorpay payment link for order {}", orderId, e);
            throw new IllegalStateException("Payment gateway error", e);
        }
    }

    @Override
    public boolean verifyWebhookSignature(String rawPayload, String signatureHeader) {
        try {
            return Utils.verifyWebhookSignature(rawPayload, signatureHeader, webhookSecret);
        } catch (RazorpayException e) {
            log.warn("Webhook signature verification failed", e);
            return false;
        }
    }

    @Override
    public String fetchPaymentLinkStatus(String gatewayLinkId) {
        try {
            PaymentLink paymentLink = razorpayClient.paymentLink.fetch(gatewayLinkId);
            return paymentLink.get("status");
        } catch (RazorpayException e) {
            log.error("Failed to fetch payment link status for {}", gatewayLinkId, e);
            return "unknown";
        }
    }
}
