package com.sivakaranam.ecommerce.notification.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service")
public interface UserClient {

    @GetMapping("/api/internal/users/{id}")
    UserClientResponse getUser(@PathVariable("id") Long id);
}
