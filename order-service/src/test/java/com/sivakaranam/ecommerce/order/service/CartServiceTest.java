package com.sivakaranam.ecommerce.order.service;

import com.sivakaranam.ecommerce.common.exception.BadRequestException;
import com.sivakaranam.ecommerce.order.client.ProductClient;
import com.sivakaranam.ecommerce.order.client.ProductClientResponse;
import com.sivakaranam.ecommerce.order.dto.AddToCartRequest;
import com.sivakaranam.ecommerce.order.model.Cart;
import com.sivakaranam.ecommerce.order.repository.CartRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ProductClient productClient;

    private CartService cartService;

    @BeforeEach
    void setUp() {
        cartService = new CartService(cartRepository, productClient);
    }

    @Test
    void addItem_withEnoughStock_addsNewLineItem() {
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(productClient.getProduct(10L))
                .thenReturn(new ProductClientResponse(10L, "Mouse", "desc", new BigDecimal("799.00"), 50, 1L, "Electronics"));

        Cart cart = cartService.addItem(1L, new AddToCartRequest(10L, 2));

        assertThat(cart.getItems()).hasSize(1);
        assertThat(cart.getItems().get(0).getQuantity()).isEqualTo(2);
        assertThat(cart.getItems().get(0).getUnitPrice()).isEqualTo(new BigDecimal("799.00"));
    }

    @Test
    void addItem_withInsufficientStock_throwsBadRequest() {
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(productClient.getProduct(10L))
                .thenReturn(new ProductClientResponse(10L, "Mouse", "desc", new BigDecimal("799.00"), 1, 1L, "Electronics"));

        assertThatThrownBy(() -> cartService.addItem(1L, new AddToCartRequest(10L, 5)))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Not enough stock");
    }
}
