package com.sivakaranam.ecommerce.order.controller;

import com.sivakaranam.ecommerce.order.dto.AddToCartRequest;
import com.sivakaranam.ecommerce.order.dto.CartResponse;
import com.sivakaranam.ecommerce.order.service.CartService;
import jakarta.validation.Valid;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public CartResponse view(JwtAuthenticationToken authentication) {
        return CartResponse.from(cartService.getOrCreateCart(userId(authentication)));
    }

    @PostMapping("/items")
    public CartResponse addItem(JwtAuthenticationToken authentication, @Valid @RequestBody AddToCartRequest request) {
        return CartResponse.from(cartService.addItem(userId(authentication), request));
    }

    @DeleteMapping("/items/{itemId}")
    public CartResponse removeItem(JwtAuthenticationToken authentication, @PathVariable Long itemId) {
        return CartResponse.from(cartService.removeItem(userId(authentication), itemId));
    }

    private Long userId(JwtAuthenticationToken authentication) {
        return Long.valueOf(authentication.getToken().getSubject());
    }
}
