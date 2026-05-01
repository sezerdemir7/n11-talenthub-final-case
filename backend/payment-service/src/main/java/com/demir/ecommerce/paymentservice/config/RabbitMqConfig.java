package com.demir.ecommerce.paymentservice.config;

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
    public Queue paymentStockReservedQueue() {
        return new Queue(RabbitMqConstants.PAYMENT_STOCK_RESERVED_QUEUE);
    }

    @Bean
    public Binding paymentStockReservedBinding() {
        return BindingBuilder
                .bind(paymentStockReservedQueue())
                .to(sagaExchange())
                .with(RabbitMqConstants.STOCK_RESERVED_ROUTING_KEY);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }



    @Bean
    public Queue paymentOrderCancelledQueue() {
        return new Queue(RabbitMqConstants.PAYMENT_ORDER_CANCELLED_QUEUE);
    }

    @Bean
    public Binding paymentOrderCancelledBinding() {
        return BindingBuilder
                .bind(paymentOrderCancelledQueue())
                .to(sagaExchange())
                .with(RabbitMqConstants.ORDER_CANCELLED_ROUTING_KEY);
    }



}
