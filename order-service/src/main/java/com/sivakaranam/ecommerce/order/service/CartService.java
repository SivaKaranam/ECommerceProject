package com.sivakaranam.ecommerce.order.service;

import com.sivakaranam.ecommerce.common.exception.BadRequestException;
import com.sivakaranam.ecommerce.common.exception.ResourceNotFoundException;
import com.sivakaranam.ecommerce.order.client.ProductClient;
import com.sivakaranam.ecommerce.order.client.ProductClientResponse;
import com.sivakaranam.ecommerce.order.dto.AddToCartRequest;
import com.sivakaranam.ecommerce.order.model.Cart;
import com.sivakaranam.ecommerce.order.model.CartItem;
import com.sivakaranam.ecommerce.order.repository.CartRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final ProductClient productClient;

    public CartService(CartRepository cartRepository, ProductClient productClient) {
        this.cartRepository = cartRepository;
        this.productClient = productClient;
    }

    public Cart getOrCreateCart(Long userId) {
        return cartRepository.findByUserId(userId).orElseGet(() -> {
            Cart cart = new Cart();
            cart.setUserId(userId);
            return cartRepository.save(cart);
        });
    }

    @Transactional
    public Cart addItem(Long userId, AddToCartRequest request) {
        Cart cart = getOrCreateCart(userId);

        ProductClientResponse product = productClient.getProduct(request.productId());
        if (product == null) {
            throw new ResourceNotFoundException("No product with id " + request.productId());
        }
        if (product.stockQuantity() < request.quantity()) {
            throw new BadRequestException("Not enough stock for product " + product.name());
        }

        cart.getItems().stream()
                .filter(item -> item.getProductId().equals(request.productId()))
                .findFirst()
                .ifPresentOrElse(
                        existing -> existing.setQuantity(existing.getQuantity() + request.quantity()),
                        () -> {
                            CartItem item = new CartItem();
                            item.setProductId(product.id());
                            item.setProductName(product.name());
                            item.setUnitPrice(product.price());
                            item.setQuantity(request.quantity());
                            cart.addItem(item);
                        }
                );

        return cartRepository.save(cart);
    }

    @Transactional
    public Cart removeItem(Long userId, Long cartItemId) {
        Cart cart = getOrCreateCart(userId);
        boolean removed = cart.getItems().removeIf(item -> item.getId().equals(cartItemId));
        if (!removed) {
            throw new ResourceNotFoundException("No cart item with id " + cartItemId);
        }
        return cartRepository.save(cart);
    }
}
