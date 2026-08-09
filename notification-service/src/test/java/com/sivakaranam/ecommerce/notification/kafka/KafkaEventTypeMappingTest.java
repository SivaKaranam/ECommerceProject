package com.sivakaranam.ecommerce.notification.kafka;

import org.apache.kafka.common.header.internals.RecordHeaders;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * order-service's OrderCreatedEvent and payment-service's PaymentCompletedEvent
 * both live under different packages than notification-service's local copies,
 * so the __TypeId__ header each producer sends needs to be remapped rather
 * than resolved by class name.
 */
class KafkaEventTypeMappingTest {

    private static final Map<String, Object> CONFIG = Map.of(
            "spring.json.trusted.packages", "com.sivakaranam.ecommerce.*",
            "spring.json.type.mapping",
            "com.sivakaranam.ecommerce.order.kafka.OrderCreatedEvent:com.sivakaranam.ecommerce.notification.kafka.OrderCreatedEvent,"
                    + "com.sivakaranam.ecommerce.payment.service.PaymentCompletedEvent:com.sivakaranam.ecommerce.notification.kafka.PaymentCompletedEvent"
    );

    @Test
    void orderCreatedEvent_withOrderServicesTypeHeader_deserializesIntoLocalCopy() {
        String json = "{\"orderId\":100,\"userId\":1,\"userEmail\":\"jane@example.com\",\"totalAmount\":49.99}";
        RecordHeaders headers = new RecordHeaders();
        headers.add("__TypeId__", "com.sivakaranam.ecommerce.order.kafka.OrderCreatedEvent".getBytes(StandardCharsets.UTF_8));

        try (JsonDeserializer<Object> deserializer = new JsonDeserializer<>()) {
            deserializer.configure(CONFIG, false);

            Object result = deserializer.deserialize("order-created", headers, json.getBytes(StandardCharsets.UTF_8));

            assertThat(result).isInstanceOf(OrderCreatedEvent.class);
            OrderCreatedEvent event = (OrderCreatedEvent) result;
            assertThat(event.orderId()).isEqualTo(100L);
            assertThat(event.userEmail()).isEqualTo("jane@example.com");
        }
    }

    @Test
    void paymentCompletedEvent_withPaymentServicesTypeHeader_deserializesIntoLocalCopy() {
        String json = "{\"orderId\":100,\"paymentId\":7,\"userId\":1,\"status\":\"SUCCESS\"}";
        RecordHeaders headers = new RecordHeaders();
        headers.add("__TypeId__", "com.sivakaranam.ecommerce.payment.service.PaymentCompletedEvent".getBytes(StandardCharsets.UTF_8));

        try (JsonDeserializer<Object> deserializer = new JsonDeserializer<>()) {
            deserializer.configure(CONFIG, false);

            Object result = deserializer.deserialize("payment-completed", headers, json.getBytes(StandardCharsets.UTF_8));

            assertThat(result).isInstanceOf(PaymentCompletedEvent.class);
            PaymentCompletedEvent event = (PaymentCompletedEvent) result;
            assertThat(event.orderId()).isEqualTo(100L);
            assertThat(event.status()).isEqualTo("SUCCESS");
        }
    }
}
