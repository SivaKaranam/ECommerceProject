package com.sivakaranam.ecommerce.notification.client;

import java.util.Set;

public record UserClientResponse(
        Long id,
        String name,
        String email,
        Set<String> roles
) {
}
