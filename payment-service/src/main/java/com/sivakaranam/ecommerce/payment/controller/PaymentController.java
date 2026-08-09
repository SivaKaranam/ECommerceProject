package com.sivakaranam.ecommerce.payment.controller;

import com.sivakaranam.ecommerce.payment.dto.CreatePaymentRequest;
import com.sivakaranam.ecommerce.payment.dto.PaymentResponse;
import com.sivakaranam.ecommerce.payment.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /** Called internally by order-service (Feign) when an order is checked out. */
    @PostMapping
    public ResponseEntity<PaymentResponse> create(@Valid @RequestBody CreatePaymentRequest request) {
        return ResponseEntity.ok(PaymentResponse.from(paymentService.createPayment(request)));
    }

    @GetMapping("/{id}")
    public PaymentResponse get(@PathVariable Long id) {
        return PaymentResponse.from(paymentService.findById(id));
    }

    /**
     * Razorpay webhook receiver. Deliberately takes the raw body as a String.
     * Signature verification is an HMAC over the exact bytes Razorpay sent, so
     * the payload can't be parsed into a DTO and re-serialized first.
     */
    @PostMapping("/webhook")
    public ResponseEntity<Void> webhook(
            @RequestBody String rawPayload,
            @RequestHeader("X-Razorpay-Signature") String signature
    ) {
        paymentService.handleWebhook(rawPayload, signature);
        return ResponseEntity.ok().build();
    }
}
