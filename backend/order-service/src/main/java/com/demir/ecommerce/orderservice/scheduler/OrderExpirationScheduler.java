package com.demir.ecommerce.orderservice.scheduler;

import com.demir.ecommerce.commonlib.event.order.OrderExpiredEvent;
import com.demir.ecommerce.commonlib.event.order.OrderItemEvent;
import com.demir.ecommerce.orderservice.entity.Order;
import com.demir.ecommerce.orderservice.entity.OrderStatus;
import com.demir.ecommerce.orderservice.messaging.OrderEventPublisher;
import com.demir.ecommerce.orderservice.repository.OrderRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class OrderExpirationScheduler {

    private static final int PAYMENT_TIMEOUT_MINUTES = 15;

    private final OrderRepository orderRepository;
    private final OrderEventPublisher orderEventPublisher;

    public OrderExpirationScheduler(OrderRepository orderRepository,
                                    OrderEventPublisher orderEventPublisher) {
        this.orderRepository = orderRepository;
        this.orderEventPublisher = orderEventPublisher;
    }

    @Scheduled(fixedRate = 300_000)
    @Transactional
    public void expireWaitingPaymentOrders() {

        LocalDateTime threshold = LocalDateTime.now()
                .minusMinutes(PAYMENT_TIMEOUT_MINUTES);

        List<Order> expiredOrders = orderRepository.findAllByStatusAndCreatedAtBefore(
                OrderStatus.WAITING_PAYMENT,
                threshold
        );

        expiredOrders.forEach(order -> {
            order.setStatus(OrderStatus.EXPIRED);
            orderRepository.save(order);

            List<OrderItemEvent> items = order.getItems().stream()
                    .map(i -> new OrderItemEvent(
                            i.getProductId(),
                            i.getProductName(),
                            i.getUnitPrice(),
                            i.getQuantity()
                    ))
                    .toList();

            orderEventPublisher.publishOrderExpired(
                    new OrderExpiredEvent(
                            order.getId(),
                            order.getUserId(),
                            items
                    )
            );
        });
    }
}
