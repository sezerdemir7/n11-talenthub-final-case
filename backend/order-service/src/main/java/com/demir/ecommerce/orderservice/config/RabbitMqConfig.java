package com.demir.ecommerce.orderservice.config;

import com.demir.ecommerce.commonlib.messaging.RabbitMqConstants;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    @Bean
    public DirectExchange sagaExchange() {
        return new DirectExchange(RabbitMqConstants.SAGA_EXCHANGE);
    }

    @Bean
    public Queue orderPaymentSucceededQueue() {
        return new Queue(RabbitMqConstants.ORDER_PAYMENT_SUCCEEDED_QUEUE);
    }

    @Bean
    public Queue orderPaymentFailedQueue() {
        return new Queue(RabbitMqConstants.ORDER_PAYMENT_FAILED_QUEUE);
    }

    @Bean
    public Queue orderStockReservationFailedQueue() {
        return new Queue(RabbitMqConstants.ORDER_STOCK_RESERVATION_FAILED_QUEUE);
    }

    @Bean
    public Binding orderPaymentSucceededBinding() {
        return BindingBuilder
                .bind(orderPaymentSucceededQueue())
                .to(sagaExchange())
                .with(RabbitMqConstants.PAYMENT_SUCCEEDED_ROUTING_KEY);
    }

    @Bean
    public Binding orderPaymentFailedBinding() {
        return BindingBuilder
                .bind(orderPaymentFailedQueue())
                .to(sagaExchange())
                .with(RabbitMqConstants.PAYMENT_FAILED_ROUTING_KEY);
    }

    @Bean
    public Binding orderStockReservationFailedBinding() {
        return BindingBuilder
                .bind(orderStockReservationFailedQueue())
                .to(sagaExchange())
                .with(RabbitMqConstants.STOCK_RESERVATION_FAILED_ROUTING_KEY);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
