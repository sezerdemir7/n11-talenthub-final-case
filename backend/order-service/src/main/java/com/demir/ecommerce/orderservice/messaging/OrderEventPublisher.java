package com.demir.ecommerce.orderservice.messaging;

import com.demir.ecommerce.commonlib.event.cart.CartClearRequestedEvent;
import com.demir.ecommerce.commonlib.event.order.OrderCancelledEvent;
import com.demir.ecommerce.commonlib.event.order.OrderCreatedEvent;
import com.demir.ecommerce.commonlib.event.order.OrderExpiredEvent;
import com.demir.ecommerce.commonlib.messaging.RabbitMqConstants;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class OrderEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public OrderEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishOrderCreated(OrderCreatedEvent event) {
        rabbitTemplate.convertAndSend(
                RabbitMqConstants.SAGA_EXCHANGE,
                RabbitMqConstants.ORDER_CREATED_ROUTING_KEY,
                event
        );
    }

    public void publishCartClearRequested(CartClearRequestedEvent event) {
        rabbitTemplate.convertAndSend(
                RabbitMqConstants.SAGA_EXCHANGE,
                RabbitMqConstants.CART_CLEAR_REQUESTED_ROUTING_KEY,
                event
        );
    }

    public void publishOrderExpired(OrderExpiredEvent event) {
        rabbitTemplate.convertAndSend(
                RabbitMqConstants.SAGA_EXCHANGE,
                RabbitMqConstants.ORDER_EXPIRED_ROUTING_KEY,
                event
        );
    }

    public void publishOrderCancelled(OrderCancelledEvent event) {
        rabbitTemplate.convertAndSend(
                RabbitMqConstants.SAGA_EXCHANGE,
                RabbitMqConstants.ORDER_CANCELLED_ROUTING_KEY,
                event
        );
    }


}
