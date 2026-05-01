package com.demir.ecommerce.paymentservice.messaging;

import com.demir.ecommerce.commonlib.event.order.OrderCancelledEvent;
import com.demir.ecommerce.commonlib.messaging.RabbitMqConstants;
import com.demir.ecommerce.paymentservice.service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentRefundEventListener {

    private static final Logger log = LoggerFactory.getLogger(PaymentRefundEventListener.class);

    private final PaymentService paymentService;

    public PaymentRefundEventListener(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @RabbitListener(queues = RabbitMqConstants.PAYMENT_ORDER_CANCELLED_QUEUE)
    public void handleOrderCancelled(OrderCancelledEvent event) {
        try {
            paymentService.refund(event.orderId());
        } catch (RuntimeException exception) {
            log.error(
                    "Refund failed for orderId={}, reason={}",
                    event.orderId(),
                    exception.getMessage()
            );
        }
    }
}
