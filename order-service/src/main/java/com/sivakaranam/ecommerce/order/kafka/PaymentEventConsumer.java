package com.sivakaranam.ecommerce.order.kafka;

import com.sivakaranam.ecommerce.order.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventConsumer.class);

    private final OrderService orderService;

    public PaymentEventConsumer(OrderService orderService) {
        this.orderService = orderService;
    }

    @KafkaListener(topics = "payment-completed", groupId = "order-service")
    public void onPaymentCompleted(PaymentCompletedEvent event) {
        log.info("Received payment-completed event for order {}: {}", event.orderId(), event.status());
        orderService.applyPaymentOutcome(event);
    }
}
