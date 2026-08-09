package com.sivakaranam.ecommerce.payment.scheduler;

import com.sivakaranam.ecommerce.payment.model.Payment;
import com.sivakaranam.ecommerce.payment.model.PaymentStatus;
import com.sivakaranam.ecommerce.payment.repository.PaymentRepository;
import com.sivakaranam.ecommerce.payment.service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Safety net for missed or delayed webhooks: any payment still PENDING a few
 * minutes after creation gets its status pulled directly from Razorpay
 * instead of waiting indefinitely on a webhook that might never arrive.
 */
@Component
public class PaymentReconciliationScheduler {

    private static final Logger log = LoggerFactory.getLogger(PaymentReconciliationScheduler.class);
    private static final int PENDING_THRESHOLD_MINUTES = 5;

    private final PaymentRepository paymentRepository;
    private final PaymentService paymentService;

    public PaymentReconciliationScheduler(PaymentRepository paymentRepository, PaymentService paymentService) {
        this.paymentRepository = paymentRepository;
        this.paymentService = paymentService;
    }

    @Scheduled(fixedDelayString = "${app.payment.reconciliation.fixed-delay-ms:120000}")
    public void reconcilePendingPayments() {
        Instant threshold = Instant.now().minus(PENDING_THRESHOLD_MINUTES, ChronoUnit.MINUTES);
        List<Payment> stalePending = paymentRepository.findByStatusAndCreatedAtBefore(PaymentStatus.PENDING, threshold);

        if (stalePending.isEmpty()) {
            return;
        }

        log.info("Reconciling {} pending payment(s) older than {} minutes", stalePending.size(), PENDING_THRESHOLD_MINUTES);
        for (Payment payment : stalePending) {
            try {
                paymentService.reconcile(payment);
            } catch (Exception e) {
                log.error("Reconciliation failed for payment {}", payment.getId(), e);
            }
        }
    }
}
