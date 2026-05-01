package com.demir.ecommerce.cartservice.config;

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
    public Queue cartClearRequestedQueue() {
        return new Queue(RabbitMqConstants.CART_CLEAR_REQUESTED_QUEUE);
    }

    @Bean
    public Binding cartClearRequestedBinding() {
        return BindingBuilder
                .bind(cartClearRequestedQueue())
                .to(sagaExchange())
                .with(RabbitMqConstants.CART_CLEAR_REQUESTED_ROUTING_KEY);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
