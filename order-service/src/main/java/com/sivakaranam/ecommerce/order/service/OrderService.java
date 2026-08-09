package com.sivakaranam.ecommerce.order.service;

import com.sivakaranam.ecommerce.common.exception.BadRequestException;
import com.sivakaranam.ecommerce.common.exception.ResourceNotFoundException;
import com.sivakaranam.ecommerce.order.client.CreatePaymentRequest;
import com.sivakaranam.ecommerce.order.client.PaymentClient;
import com.sivakaranam.ecommerce.order.client.PaymentClientResponse;
import com.sivakaranam.ecommerce.order.kafka.OrderCreatedEvent;
import com.sivakaranam.ecommerce.order.kafka.OrderEventProducer;
import com.sivakaranam.ecommerce.order.kafka.PaymentCompletedEvent;
import com.sivakaranam.ecommerce.order.model.Cart;
import com.sivakaranam.ecommerce.order.model.Order;
import com.sivakaranam.ecommerce.order.model.OrderItem;
import com.sivakaranam.ecommerce.order.model.OrderStatus;
import com.sivakaranam.ecommerce.order.repository.CartRepository;
import com.sivakaranam.ecommerce.order.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final CartService cartService;
    private final PaymentClient paymentClient;
    private final OrderEventProducer orderEventProducer;

    public OrderService(
            OrderRepository orderRepository,
            CartRepository cartRepository,
            CartService cartService,
            PaymentClient paymentClient,
            OrderEventProducer orderEventProducer
    ) {
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.cartService = cartService;
        this.paymentClient = paymentClient;
        this.orderEventProducer = orderEventProducer;
    }

    @Transactional
    public Order checkout(Long userId, String userEmail) {
        Cart cart = cartService.getOrCreateCart(userId);
        if (cart.getItems().isEmpty()) {
            throw new BadRequestException("Cannot check out an empty cart");
        }

        Order order = new Order();
        order.setUserId(userId);
        order.setStatus(OrderStatus.CREATED);

        BigDecimal total = BigDecimal.ZERO;
        for (var cartItem : cart.getItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(cartItem.getProductId());
            orderItem.setProductName(cartItem.getProductName());
            orderItem.setUnitPrice(cartItem.getUnitPrice());
            orderItem.setQuantity(cartItem.getQuantity());
            order.addItem(orderItem);
            total = total.add(cartItem.getUnitPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
        }
        order.setTotalAmount(total);

        Order savedOrder = orderRepository.save(order);

        PaymentClientResponse payment = paymentClient.createPayment(
                new CreatePaymentRequest(savedOrder.getId(), userId, total)
        );
        savedOrder.setPaymentId(payment.paymentId().toString());
        savedOrder.setStatus(OrderStatus.PAYMENT_PENDING);
        orderRepository.save(savedOrder);

        cart.clear();
        cartRepository.save(cart);

        orderEventProducer.publishOrderCreated(new OrderCreatedEvent(savedOrder.getId(), userId, userEmail, total));

        return savedOrder;
    }

    public Page<Order> listOrders(Long userId, Pageable pageable) {
        return orderRepository.findByUserId(userId, pageable);
    }

    public Order getOrder(Long userId, Long orderId) {
        return orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("No order with id " + orderId));
    }

    /**
     * Consumed off Kafka when payment-service finishes processing a gateway
     * webhook. This is the reconciliation step that brings the order's status
     * in line with what actually happened to the payment.
     */
    @Transactional
    public void applyPaymentOutcome(PaymentCompletedEvent event) {
        Order order = orderRepository.findByPaymentId(event.paymentId().toString())
                .orElse(null);

        if (order == null) {
            log.warn("Received payment outcome for unknown paymentId {}", event.paymentId());
            return;
        }

        order.setStatus("SUCCESS".equalsIgnoreCase(event.status()) ? OrderStatus.PAID : OrderStatus.PAYMENT_FAILED);
        orderRepository.save(order);
    }
}
