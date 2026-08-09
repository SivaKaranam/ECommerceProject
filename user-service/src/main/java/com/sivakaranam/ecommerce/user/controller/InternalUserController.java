package com.sivakaranam.ecommerce.user.controller;

import com.sivakaranam.ecommerce.user.dto.UserResponse;
import com.sivakaranam.ecommerce.user.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Service-to-service lookups only (e.g. notification-service resolving an
 * email address by user id). Kept on its own path, permitAll in
 * SecurityConfig, and never routed through the API Gateway, the same
 * internal-trust tradeoff as payment-service's POST /api/payments: a real
 * deployment would put a service credential or network policy in front of it.
 */
@RestController
@RequestMapping("/api/internal/users")
public class InternalUserController {

    private final UserService userService;

    public InternalUserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{id}")
    public UserResponse getById(@PathVariable Long id) {
        return UserResponse.from(userService.findById(id));
    }
}
