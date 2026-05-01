package com.demir.ecommerce.cartservice.messaging;

import com.demir.ecommerce.cartservice.service.CartService;
import com.demir.ecommerce.commonlib.event.cart.CartClearRequestedEvent;
import com.demir.ecommerce.commonlib.messaging.RabbitMqConstants;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class CartSagaEventListener {

    private final CartService cartService;

    public CartSagaEventListener(CartService cartService) {
        this.cartService = cartService;
    }


    @RabbitListener(queues = RabbitMqConstants.CART_CLEAR_REQUESTED_QUEUE)
    public void handleCartClearRequested(CartClearRequestedEvent event) {
        cartService.clearCart(event.userId());
    }

}
