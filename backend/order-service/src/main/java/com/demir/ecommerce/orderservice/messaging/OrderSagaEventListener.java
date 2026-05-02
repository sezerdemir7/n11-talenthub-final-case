package com.demir.ecommerce.orderservice.messaging;

import com.demir.ecommerce.commonlib.event.cart.CartClearRequestedEvent;
import com.demir.ecommerce.commonlib.event.order.OrderCreatedEvent;
import com.demir.ecommerce.commonlib.event.order.OrderItemEvent;
import com.demir.ecommerce.commonlib.event.payment.PaymentFailedEvent;
import com.demir.ecommerce.commonlib.event.payment.PaymentSucceededEvent;
import com.demir.ecommerce.commonlib.event.stock.StockReservationFailedEvent;
import com.demir.ecommerce.commonlib.messaging.RabbitMqConstants;
import com.demir.ecommerce.orderservice.entity.Order;
import com.demir.ecommerce.orderservice.entity.OrderItem;
import com.demir.ecommerce.orderservice.entity.OrderStatus;
import com.demir.ecommerce.orderservice.repository.OrderRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrderSagaEventListener {

    private final OrderRepository orderRepository;
    private final OrderEventPublisher orderEventPublisher;

    public OrderSagaEventListener(OrderRepository orderRepository,
                                  OrderEventPublisher orderEventPublisher) {
        this.orderRepository = orderRepository;
        this.orderEventPublisher = orderEventPublisher;
    }

    @RabbitListener(queues = RabbitMqConstants.ORDER_PAYMENT_SUCCEEDED_QUEUE)
    public void handlePaymentSucceeded(PaymentSucceededEvent event) {

        Order order = orderRepository.findWithItemsById(event.orderId())
                .orElse(null);

        if (order == null) {
            return;
        }

        if (order.getStatus() != OrderStatus.WAITING_PAYMENT) {
            return;
        }

        order.setStatus(OrderStatus.CONFIRMED);
        orderRepository.save(order);

        orderEventPublisher.publishOrderCreated(toOrderCreatedEvent(order));

        List<Long> productIds = order.getItems().stream()
                .map(OrderItem::getProductId)
                .toList();

        orderEventPublisher.publishCartClearRequested(
                new CartClearRequestedEvent(
                        order.getId(),
                        order.getUserId(),
                        productIds
                )
        );
    }

    @RabbitListener(queues = RabbitMqConstants.ORDER_PAYMENT_FAILED_QUEUE)
    public void handlePaymentFailed(PaymentFailedEvent event) {

        Order order = orderRepository.findById(event.orderId())
                .orElse(null);

        if (order == null) {
            return;
        }

        if (order.getStatus() != OrderStatus.WAITING_PAYMENT) {
            return;
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
    }

    @RabbitListener(queues = RabbitMqConstants.ORDER_STOCK_RESERVATION_FAILED_QUEUE)
    public void handleStockReservationFailed(StockReservationFailedEvent event) {

        Order order = orderRepository.findById(event.orderId())
                .orElse(null);

        if (order == null) {
            return;
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
    }

    private OrderCreatedEvent toOrderCreatedEvent(Order order) {
        List<OrderItemEvent> items = order.getItems().stream()
                .map(i -> new OrderItemEvent(
                        i.getProductId(),
                        i.getProductName(),
                        i.getUnitPrice(),
                        i.getQuantity()
                ))
                .toList();

        return new OrderCreatedEvent(
                order.getId(),
                order.getUserId(),
                order.getTotalPrice(),
                items
        );
    }
}
