package com.demir.ecommerce.paymentservice.messaging;

import com.demir.ecommerce.commonlib.event.payment.PaymentFailedEvent;
import com.demir.ecommerce.commonlib.event.payment.PaymentSucceededEvent;
import com.demir.ecommerce.commonlib.messaging.RabbitMqConstants;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public PaymentEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishPaymentSucceeded(PaymentSucceededEvent event) {
        rabbitTemplate.convertAndSend(
                RabbitMqConstants.SAGA_EXCHANGE,
                RabbitMqConstants.PAYMENT_SUCCEEDED_ROUTING_KEY,
                event
        );
    }

    public void publishPaymentFailed(PaymentFailedEvent event) {
        rabbitTemplate.convertAndSend(
                RabbitMqConstants.SAGA_EXCHANGE,
                RabbitMqConstants.PAYMENT_FAILED_ROUTING_KEY,
                event
        );
    }
}
