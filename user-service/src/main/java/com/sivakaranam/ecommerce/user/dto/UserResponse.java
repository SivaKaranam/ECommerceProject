package com.sivakaranam.ecommerce.user.dto;

import com.sivakaranam.ecommerce.user.model.User;

import java.util.Set;
import java.util.stream.Collectors;

public record UserResponse(
        Long id,
        String name,
        String email,
        Set<String> roles
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRoles().stream().map(role -> role.getName()).collect(Collectors.toSet())
        );
    }
}
