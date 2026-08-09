package com.sivakaranam.ecommerce.order.kafka;

import org.apache.kafka.common.header.internals.RecordHeaders;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * payment-service's PaymentCompletedEvent lives under a different package
 * than order-service's local copy, so the __TypeId__ header payment-service
 * sends needs to be remapped rather than resolved by class name.
 */
class KafkaEventTypeMappingTest {

    @Test
    void paymentCompletedEvent_withPaymentServicesTypeHeader_deserializesIntoLocalCopy() {
        String json = "{\"orderId\":100,\"paymentId\":7,\"userId\":1,\"status\":\"SUCCESS\"}";
        RecordHeaders headers = new RecordHeaders();
        headers.add("__TypeId__", "com.sivakaranam.ecommerce.payment.service.PaymentCompletedEvent".getBytes(StandardCharsets.UTF_8));

        try (JsonDeserializer<Object> deserializer = new JsonDeserializer<>()) {
            deserializer.configure(Map.of(
                    "spring.json.trusted.packages", "com.sivakaranam.ecommerce.*",
                    "spring.json.type.mapping",
                    "com.sivakaranam.ecommerce.payment.service.PaymentCompletedEvent:com.sivakaranam.ecommerce.order.kafka.PaymentCompletedEvent"
            ), false);

            Object result = deserializer.deserialize("payment-completed", headers, json.getBytes(StandardCharsets.UTF_8));

            assertThat(result).isInstanceOf(PaymentCompletedEvent.class);
            PaymentCompletedEvent event = (PaymentCompletedEvent) result;
            assertThat(event.orderId()).isEqualTo(100L);
            assertThat(event.status()).isEqualTo("SUCCESS");
        }
    }
}
