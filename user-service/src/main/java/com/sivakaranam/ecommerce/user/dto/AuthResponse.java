package com.sivakaranam.ecommerce.user.dto;

public record AuthResponse(
        String accessToken,
        String tokenType,
        long expiresInSeconds
) {
}
