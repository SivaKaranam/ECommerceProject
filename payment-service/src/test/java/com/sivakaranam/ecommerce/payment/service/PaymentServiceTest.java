package com.sivakaranam.ecommerce.payment.service;

import com.sivakaranam.ecommerce.common.exception.BadRequestException;
import com.sivakaranam.ecommerce.payment.dto.CreatePaymentRequest;
import com.sivakaranam.ecommerce.payment.gateway.GatewayPaymentLink;
import com.sivakaranam.ecommerce.payment.gateway.PaymentGateway;
import com.sivakaranam.ecommerce.payment.model.Payment;
import com.sivakaranam.ecommerce.payment.model.PaymentStatus;
import com.sivakaranam.ecommerce.payment.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentGateway mockGateway;

    @Mock
    private PaymentEventProducer paymentEventProducer;

    private PaymentService paymentServiceWithMock;

    @BeforeEach
    void setUp() {
        paymentServiceWithMock = new PaymentService(paymentRepository, mockGateway, paymentEventProducer);
    }

    @Test
    void createPayment_createsGatewayLinkAndSavesPendingPayment() {
        when(paymentRepository.findByOrderId(100L)).thenReturn(Optional.empty());
        when(mockGateway.createPaymentLink(100L, new BigDecimal("1598.00")))
                .thenReturn(new GatewayPaymentLink("plink_abc", "https://razorpay.test/plink_abc"));

        AtomicLong idGenerator = new AtomicLong(1);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            if (payment.getId() == null) {
                payment.setId(idGenerator.getAndIncrement());
            }
            return payment;
        });

        Payment payment = paymentServiceWithMock.createPayment(new CreatePaymentRequest(100L, 1L, new BigDecimal("1598.00")));

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(payment.getGatewayLinkId()).isEqualTo("plink_abc");
        assertThat(payment.getGatewayLinkUrl()).isEqualTo("https://razorpay.test/plink_abc");
    }

    @Test
    void createPayment_whenOrderAlreadyHasAPayment_throwsBadRequest() {
        when(paymentRepository.findByOrderId(100L)).thenReturn(Optional.of(new Payment()));

        assertThatThrownBy(() -> paymentServiceWithMock.createPayment(new CreatePaymentRequest(100L, 1L, BigDecimal.TEN)))
                .isInstanceOf(BadRequestException.class);

        verifyNoInteractions(mockGateway);
    }

    @Test
    void handleWebhook_withValidSignatureAndPaidStatus_marksPaymentSuccessAndPublishesEvent() {
        Payment payment = new Payment();
        payment.setId(7L);
        payment.setOrderId(100L);
        payment.setUserId(1L);
        payment.setGatewayLinkId("plink_abc");
        payment.setStatus(PaymentStatus.PENDING);

        when(mockGateway.verifyWebhookSignature(anyString(), eq("sig123"))).thenReturn(true);
        when(paymentRepository.findByGatewayLinkId("plink_abc")).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        String payload = """
                {
                  "event": "payment_link.paid",
                  "payload": {
                    "payment_link": { "entity": { "id": "plink_abc", "reference_id": "100", "status": "paid" } },
                    "payment": { "entity": { "id": "pay_xyz", "status": "captured" } }
                  }
                }
                """;

        paymentServiceWithMock.handleWebhook(payload, "sig123");

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
        assertThat(payment.getGatewayPaymentId()).isEqualTo("pay_xyz");
        verify(paymentEventProducer).publishPaymentCompleted(
                new PaymentCompletedEvent(100L, 7L, 1L, "SUCCESS")
        );
    }

    @Test
    void handleWebhook_withInvalidSignature_throwsBadRequest_andNeverTouchesTheRepository() {
        when(mockGateway.verifyWebhookSignature(anyString(), eq("bad-sig"))).thenReturn(false);

        assertThatThrownBy(() -> paymentServiceWithMock.handleWebhook("{}", "bad-sig"))
                .isInstanceOf(BadRequestException.class);

        verifyNoInteractions(paymentRepository, paymentEventProducer);
    }

    @Test
    void reconcile_usingFakeGateway_picksUpStatusChangeWithoutStubbingEachCall() {
        // Demonstrates a fake test double instead of a mock: the fake actually
        // tracks link status in memory, so this test exercises create-then-fetch
        // as a real sequence rather than pre-programming each return value.
        FakePaymentGateway fakeGateway = new FakePaymentGateway();
        PaymentService paymentServiceWithFake = new PaymentService(paymentRepository, fakeGateway, paymentEventProducer);

        AtomicLong idGenerator = new AtomicLong(1);
        when(paymentRepository.findByOrderId(any())).thenReturn(Optional.empty());
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            if (payment.getId() == null) {
                payment.setId(idGenerator.getAndIncrement());
            }
            return payment;
        });

        Payment payment = paymentServiceWithFake.createPayment(new CreatePaymentRequest(200L, 1L, BigDecimal.TEN));
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PENDING);

        fakeGateway.markPaid(payment.getGatewayLinkId());
        paymentServiceWithFake.reconcile(payment);

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
        verify(paymentEventProducer).publishPaymentCompleted(any());
    }
}
