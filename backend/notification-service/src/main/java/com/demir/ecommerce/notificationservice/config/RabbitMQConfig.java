package com.demir.ecommerce.notificationservice.config;

import com.demir.ecommerce.commonlib.messaging.RabbitMqConstants;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Bean
    public DirectExchange sagaExchange() {
        return new DirectExchange(RabbitMqConstants.SAGA_EXCHANGE);
    }

    @Bean
    public Queue notificationOrderCreatedQueue() {
        return new Queue(RabbitMqConstants.NOTIFICATION_ORDER_CREATED_QUEUE, true);
    }

    @Bean
    public Queue notificationOrderCancelledQueue() {
        return new Queue(RabbitMqConstants.NOTIFICATION_ORDER_CANCELLED_QUEUE, true);
    }

    @Bean
    public Binding notificationOrderCreatedBinding() {
        return BindingBuilder
                .bind(notificationOrderCreatedQueue())
                .to(sagaExchange())
                .with(RabbitMqConstants.ORDER_CREATED_ROUTING_KEY);
    }

    @Bean
    public Binding notificationOrderCancelledBinding() {
        return BindingBuilder
                .bind(notificationOrderCancelledQueue())
                .to(sagaExchange())
                .with(RabbitMqConstants.ORDER_CANCELLED_ROUTING_KEY);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}