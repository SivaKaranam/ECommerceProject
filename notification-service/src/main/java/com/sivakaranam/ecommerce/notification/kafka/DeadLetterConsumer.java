package com.sivakaranam.ecommerce.notification.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/** Where messages land after exhausting retries, logged for now. A real deployment would alert on this. */
@Component
public class DeadLetterConsumer {

    private static final Logger log = LoggerFactory.getLogger(DeadLetterConsumer.class);

    @KafkaListener(topics = {"order-created.DLT", "payment-completed.DLT"}, groupId = "notification-service-dlt")
    public void onDeadLetter(Object payload) {
        log.error("Message moved to dead-letter topic after repeated processing failures: {}", payload);
    }
}
