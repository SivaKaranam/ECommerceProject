package com.sivakaranam.ecommerce.payment.repository;

import com.sivakaranam.ecommerce.payment.model.Payment;
import com.sivakaranam.ecommerce.payment.model.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByOrderId(Long orderId);

    Optional<Payment> findByGatewayLinkId(String gatewayLinkId);

    List<Payment> findByStatusAndCreatedAtBefore(PaymentStatus status, Instant threshold);
}
