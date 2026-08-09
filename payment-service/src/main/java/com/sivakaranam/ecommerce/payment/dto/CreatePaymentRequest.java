package com.sivakaranam.ecommerce.payment.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreatePaymentRequest(
        @NotNull Long orderId,
        @NotNull Long userId,
        @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal amount
) {
}
