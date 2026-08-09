package com.sivakaranam.ecommerce.order.service;

import com.sivakaranam.ecommerce.common.exception.BadRequestException;
import com.sivakaranam.ecommerce.order.client.PaymentClient;
import com.sivakaranam.ecommerce.order.client.PaymentClientResponse;
import com.sivakaranam.ecommerce.order.kafka.OrderEventProducer;
import com.sivakaranam.ecommerce.order.kafka.PaymentCompletedEvent;
import com.sivakaranam.ecommerce.order.model.Cart;
import com.sivakaranam.ecommerce.order.model.CartItem;
import com.sivakaranam.ecommerce.order.model.Order;
import com.sivakaranam.ecommerce.order.model.OrderStatus;
import com.sivakaranam.ecommerce.order.repository.CartRepository;
import com.sivakaranam.ecommerce.order.repository.OrderRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartService cartService;

    @Mock
    private PaymentClient paymentClient;

    @Mock
    private OrderEventProducer orderEventProducer;

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(orderRepository, cartRepository, cartService, paymentClient, orderEventProducer);
    }

    @Test
    void checkout_withEmptyCart_throwsBadRequest() {
        Cart emptyCart = new Cart();
        emptyCart.setUserId(1L);
        when(cartService.getOrCreateCart(1L)).thenReturn(emptyCart);

        assertThatThrownBy(() -> orderService.checkout(1L, "user@example.com"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("empty cart");

        verifyNoInteractions(paymentClient, orderEventProducer);
    }

    @Test
    void checkout_withItems_createsOrderAndInitiatesPayment() {
        Cart cart = new Cart();
        cart.setUserId(1L);
        CartItem item = new CartItem();
        item.setProductId(10L);
        item.setProductName("Mouse");
        item.setUnitPrice(new BigDecimal("799.00"));
        item.setQuantity(2);
        cart.addItem(item);

        when(cartService.getOrCreateCart(1L)).thenReturn(cart);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            if (order.getId() == null) {
                order.setId(100L);
            }
            return order;
        });
        when(paymentClient.createPayment(any())).thenReturn(new PaymentClientResponse(555L, "https://pay.example/555", "PENDING"));

        Order order = orderService.checkout(1L, "user@example.com");

        assertThat(order.getId()).isEqualTo(100L);
        assertThat(order.getTotalAmount()).isEqualTo(new BigDecimal("1598.00"));
        assertThat(order.getPaymentId()).isEqualTo("555");
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAYMENT_PENDING);
        assertThat(cart.getItems()).isEmpty();

        verify(orderEventProducer).publishOrderCreated(any());
    }

    @Test
    void applyPaymentOutcome_marksOrderPaid_onSuccess() {
        Order order = new Order();
        order.setId(100L);
        order.setPaymentId("555");
        order.setStatus(OrderStatus.PAYMENT_PENDING);

        when(orderRepository.findByPaymentId("555")).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        orderService.applyPaymentOutcome(new PaymentCompletedEvent(100L, 555L, 1L, "SUCCESS"));

        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
    }

    @Test
    void applyPaymentOutcome_marksOrderFailed_onFailure() {
        Order order = new Order();
        order.setId(100L);
        order.setPaymentId("555");
        order.setStatus(OrderStatus.PAYMENT_PENDING);

        when(orderRepository.findByPaymentId("555")).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        orderService.applyPaymentOutcome(new PaymentCompletedEvent(100L, 555L, 1L, "FAILED"));

        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAYMENT_FAILED);
    }
}
