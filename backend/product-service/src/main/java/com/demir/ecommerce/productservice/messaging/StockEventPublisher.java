package com.demir.ecommerce.productservice.messaging;

import com.demir.ecommerce.commonlib.event.stock.StockReservationFailedEvent;
import com.demir.ecommerce.commonlib.event.stock.StockReservedEvent;
import com.demir.ecommerce.commonlib.messaging.RabbitMqConstants;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class StockEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public StockEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishStockReserved(StockReservedEvent event) {
        rabbitTemplate.convertAndSend(
                RabbitMqConstants.SAGA_EXCHANGE,
                RabbitMqConstants.STOCK_RESERVED_ROUTING_KEY,
                event
        );
    }

    public void publishStockReservationFailed(StockReservationFailedEvent event) {
        rabbitTemplate.convertAndSend(
                RabbitMqConstants.SAGA_EXCHANGE,
                RabbitMqConstants.STOCK_RESERVATION_FAILED_ROUTING_KEY,
                event
        );
    }
}
