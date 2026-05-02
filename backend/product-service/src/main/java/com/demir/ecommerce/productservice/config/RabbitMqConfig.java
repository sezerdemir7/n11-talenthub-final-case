package com.demir.ecommerce.productservice.config;

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
    public Queue productOrderCreatedQueue() {
        return new Queue(RabbitMqConstants.PRODUCT_ORDER_CREATED_QUEUE);
    }

    @Bean
    public Queue productPaymentFailedQueue() {
        return new Queue(RabbitMqConstants.PRODUCT_PAYMENT_FAILED_QUEUE);
    }

    @Bean
    public Binding productOrderCreatedBinding() {
        return BindingBuilder
                .bind(productOrderCreatedQueue())
                .to(sagaExchange())
                .with(RabbitMqConstants.ORDER_CREATED_ROUTING_KEY);
    }

    @Bean
    public Binding productPaymentFailedBinding() {
        return BindingBuilder
                .bind(productPaymentFailedQueue())
                .to(sagaExchange())
                .with(RabbitMqConstants.PAYMENT_FAILED_ROUTING_KEY);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public Queue productSellerSuspendedQueue() {
        return new Queue(RabbitMqConstants.PRODUCT_SELLER_SUSPENDED_QUEUE);
    }

    @Bean
    public Binding productSellerSuspendedBinding() {
        return BindingBuilder
                .bind(productSellerSuspendedQueue())
                .to(sagaExchange())
                .with(RabbitMqConstants.SELLER_SUSPENDED_ROUTING_KEY);
    }

    @Bean
    public Queue productOrderExpiredQueue() {
        return new Queue(RabbitMqConstants.PRODUCT_ORDER_EXPIRED_QUEUE);
    }
    @Bean
    public Binding productOrderExpiredBinding() {
        return BindingBuilder
                .bind(productOrderExpiredQueue())
                .to(sagaExchange())
                .with(RabbitMqConstants.ORDER_EXPIRED_ROUTING_KEY);
    }


    @Bean
    public Queue productOrderCancelledQueue() {
        return new Queue(RabbitMqConstants.PRODUCT_ORDER_CANCELLED_QUEUE);
    }

    @Bean
    public Binding productOrderCancelledBinding() {
        return BindingBuilder
                .bind(productOrderCancelledQueue())
                .to(sagaExchange())
                .with(RabbitMqConstants.ORDER_CANCELLED_ROUTING_KEY);
    }


    @Bean
    public Queue productSellerActivatedQueue() {
        return new Queue(RabbitMqConstants.PRODUCT_SELLER_ACTIVATED_QUEUE);
    }

    @Bean
    public Binding productSellerActivatedBinding() {
        return BindingBuilder
                .bind(productSellerActivatedQueue())
                .to(sagaExchange())
                .with(RabbitMqConstants.SELLER_ACTIVATED_ROUTING_KEY);
    }



}
