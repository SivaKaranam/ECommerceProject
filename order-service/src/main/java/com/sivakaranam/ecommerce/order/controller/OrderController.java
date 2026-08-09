package com.sivakaranam.ecommerce.order.controller;

import com.sivakaranam.ecommerce.order.dto.OrderResponse;
import com.sivakaranam.ecommerce.order.service.OrderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public OrderResponse checkout(JwtAuthenticationToken authentication) {
        String email = authentication.getToken().getClaimAsString("email");
        return OrderResponse.from(orderService.checkout(userId(authentication), email));
    }

    @GetMapping
    public Page<OrderResponse> list(JwtAuthenticationToken authentication, @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return orderService.listOrders(userId(authentication), pageable).map(OrderResponse::from);
    }

    @GetMapping("/{id}")
    public OrderResponse get(JwtAuthenticationToken authentication, @PathVariable Long id) {
        return OrderResponse.from(orderService.getOrder(userId(authentication), id));
    }

    private Long userId(JwtAuthenticationToken authentication) {
        return Long.valueOf(authentication.getToken().getSubject());
    }
}
