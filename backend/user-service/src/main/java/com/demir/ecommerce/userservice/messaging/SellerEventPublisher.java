package com.demir.ecommerce.userservice.messaging;

import com.demir.ecommerce.commonlib.event.seller.SellerSuspendedEvent;
import com.demir.ecommerce.commonlib.messaging.RabbitMqConstants;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class SellerEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public SellerEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishSellerSuspended(SellerSuspendedEvent event) {
        rabbitTemplate.convertAndSend(
                RabbitMqConstants.SAGA_EXCHANGE,
                RabbitMqConstants.SELLER_SUSPENDED_ROUTING_KEY,
                event
        );
    }
}
