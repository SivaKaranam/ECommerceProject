package com.sivakaranam.ecommerce.notification.kafka;

import com.sivakaranam.ecommerce.notification.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderEventListener {

    private static final Logger log = LoggerFactory.getLogger(OrderEventListener.class);

    private final EmailService emailService;

    public OrderEventListener(EmailService emailService) {
        this.emailService = emailService;
    }

    @KafkaListener(topics = "order-created", groupId = "notification-service")
    public void onOrderCreated(OrderCreatedEvent event) {
        log.info("Received order-created event for order {}", event.orderId());
        emailService.send(
                event.userEmail(),
                "Order #" + event.orderId() + " confirmed",
                "Thanks for your order! Total: " + event.totalAmount()
        );
    }
}
