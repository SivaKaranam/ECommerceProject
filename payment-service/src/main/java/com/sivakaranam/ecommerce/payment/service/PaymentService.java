package com.sivakaranam.ecommerce.payment.service;

import com.sivakaranam.ecommerce.common.exception.BadRequestException;
import com.sivakaranam.ecommerce.common.exception.ResourceNotFoundException;
import com.sivakaranam.ecommerce.payment.dto.CreatePaymentRequest;
import com.sivakaranam.ecommerce.payment.gateway.GatewayPaymentLink;
import com.sivakaranam.ecommerce.payment.gateway.PaymentGateway;
import com.sivakaranam.ecommerce.payment.model.Payment;
import com.sivakaranam.ecommerce.payment.model.PaymentStatus;
import com.sivakaranam.ecommerce.payment.repository.PaymentRepository;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository paymentRepository;
    private final PaymentGateway paymentGateway;
    private final PaymentEventProducer paymentEventProducer;

    public PaymentService(PaymentRepository paymentRepository, PaymentGateway paymentGateway, PaymentEventProducer paymentEventProducer) {
        this.paymentRepository = paymentRepository;
        this.paymentGateway = paymentGateway;
        this.paymentEventProducer = paymentEventProducer;
    }

    @Transactional
    public Payment createPayment(CreatePaymentRequest request) {
        paymentRepository.findByOrderId(request.orderId()).ifPresent(existing -> {
            throw new BadRequestException("A payment already exists for order " + request.orderId());
        });

        GatewayPaymentLink link = paymentGateway.createPaymentLink(request.orderId(), request.amount());

        Payment payment = new Payment();
        payment.setOrderId(request.orderId());
        payment.setUserId(request.userId());
        payment.setAmount(request.amount());
        payment.setStatus(PaymentStatus.PENDING);
        payment.setGatewayLinkId(link.id());
        payment.setGatewayLinkUrl(link.shortUrl());

        return paymentRepository.save(payment);
    }

    public Payment findById(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No payment with id " + id));
    }

    /**
     * Verifies the webhook signature against the raw request body (Razorpay's
     * HMAC covers the exact bytes sent, so this must run before any JSON
     * parsing/re-serialization touches the payload), then reconciles our
     * Payment record and tells order-service the outcome over Kafka.
     */
    @Transactional
    public void handleWebhook(String rawPayload, String signatureHeader) {
        if (!paymentGateway.verifyWebhookSignature(rawPayload, signatureHeader)) {
            throw new BadRequestException("Invalid webhook signature");
        }

        JSONObject root = new JSONObject(rawPayload);
        JSONObject paymentLinkEntity = root.getJSONObject("payload").getJSONObject("payment_link").getJSONObject("entity");

        String gatewayLinkId = paymentLinkEntity.getString("id");
        String linkStatus = paymentLinkEntity.getString("status");

        String gatewayPaymentId = null;
        if (root.getJSONObject("payload").has("payment")) {
            gatewayPaymentId = root.getJSONObject("payload").getJSONObject("payment").getJSONObject("entity").optString("id", null);
        }

        Payment payment = paymentRepository.findByGatewayLinkId(gatewayLinkId)
                .orElseThrow(() -> new ResourceNotFoundException("No payment for gateway link " + gatewayLinkId));

        applyGatewayStatus(payment, linkStatus, gatewayPaymentId);
    }

    /** Runs on a schedule to catch payments the webhook never confirmed (missed delivery, gateway hiccup). */
    @Transactional
    public void reconcile(Payment payment) {
        String status = paymentGateway.fetchPaymentLinkStatus(payment.getGatewayLinkId());
        applyGatewayStatus(payment, status, null);
    }

    private void applyGatewayStatus(Payment payment, String gatewayStatus, String gatewayPaymentId) {
        PaymentStatus previousStatus = payment.getStatus();
        PaymentStatus newStatus = switch (gatewayStatus) {
            case "paid" -> PaymentStatus.SUCCESS;
            case "cancelled", "expired" -> PaymentStatus.FAILED;
            default -> PaymentStatus.PENDING;
        };

        if (newStatus == previousStatus) {
            return; // already reconciled, avoid re-publishing the same event
        }

        payment.setStatus(newStatus);
        if (gatewayPaymentId != null) {
            payment.setGatewayPaymentId(gatewayPaymentId);
        }
        paymentRepository.save(payment);

        if (newStatus == PaymentStatus.SUCCESS || newStatus == PaymentStatus.FAILED) {
            paymentEventProducer.publishPaymentCompleted(
                    new PaymentCompletedEvent(payment.getOrderId(), payment.getId(), payment.getUserId(), newStatus.name())
            );
        }
    }
}
