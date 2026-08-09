package com.sivakaranam.ecommerce.notification.kafka;

import com.sivakaranam.ecommerce.notification.client.UserClient;
import com.sivakaranam.ecommerce.notification.client.UserClientResponse;
import com.sivakaranam.ecommerce.notification.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventListener {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventListener.class);

    private final EmailService emailService;
    private final UserClient userClient;

    public PaymentEventListener(EmailService emailService, UserClient userClient) {
        this.emailService = emailService;
        this.userClient = userClient;
    }

    @KafkaListener(topics = "payment-completed", groupId = "notification-service")
    public void onPaymentCompleted(PaymentCompletedEvent event) {
        log.info("Received payment-completed event for order {}: {}", event.orderId(), event.status());

        UserClientResponse user = userClient.getUser(event.userId());
        boolean success = "SUCCESS".equalsIgnoreCase(event.status());

        emailService.send(
                user.email(),
                success ? "Payment received for order #" + event.orderId() : "Payment failed for order #" + event.orderId(),
                success
                        ? "Your payment for order #" + event.orderId() + " was successful. Thank you!"
                        : "Your payment for order #" + event.orderId() + " could not be processed. Please try again."
        );
    }
}
