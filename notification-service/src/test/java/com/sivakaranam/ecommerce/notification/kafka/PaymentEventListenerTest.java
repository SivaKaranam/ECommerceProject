package com.sivakaranam.ecommerce.notification.kafka;

import com.sivakaranam.ecommerce.notification.client.UserClient;
import com.sivakaranam.ecommerce.notification.client.UserClientResponse;
import com.sivakaranam.ecommerce.notification.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentEventListenerTest {

    @Mock
    private EmailService emailService;

    @Mock
    private UserClient userClient;

    private PaymentEventListener listener;

    @BeforeEach
    void setUp() {
        listener = new PaymentEventListener(emailService, userClient);
    }

    @Test
    void onPaymentCompleted_withSuccessStatus_sendsSuccessEmail() {
        when(userClient.getUser(1L)).thenReturn(new UserClientResponse(1L, "Jane", "jane@example.com", Set.of("CUSTOMER")));

        listener.onPaymentCompleted(new PaymentCompletedEvent(100L, 7L, 1L, "SUCCESS"));

        verify(emailService).send(eq("jane@example.com"), contains("Payment received"), contains("successful"));
    }

    @Test
    void onPaymentCompleted_withFailedStatus_sendsFailureEmail() {
        when(userClient.getUser(1L)).thenReturn(new UserClientResponse(1L, "Jane", "jane@example.com", Set.of("CUSTOMER")));

        listener.onPaymentCompleted(new PaymentCompletedEvent(100L, 7L, 1L, "FAILED"));

        verify(emailService).send(eq("jane@example.com"), contains("Payment failed"), contains("not be processed"));
    }
}
