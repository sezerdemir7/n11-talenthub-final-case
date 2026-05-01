package com.demir.ecommerce.productservice.messaging;

import com.demir.ecommerce.commonlib.event.order.OrderCancelledEvent;
import com.demir.ecommerce.commonlib.event.order.OrderCreatedEvent;
import com.demir.ecommerce.commonlib.event.order.OrderExpiredEvent;
import com.demir.ecommerce.commonlib.event.order.OrderItemEvent;
import com.demir.ecommerce.commonlib.event.payment.PaymentFailedEvent;
import com.demir.ecommerce.commonlib.event.stock.StockReservationFailedEvent;
import com.demir.ecommerce.commonlib.event.stock.StockReservedEvent;
import com.demir.ecommerce.commonlib.messaging.RabbitMqConstants;
import com.demir.ecommerce.productservice.entity.Product;
import com.demir.ecommerce.productservice.repository.ProductRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class ProductSagaEventListener {

    private final ProductRepository productRepository;
    private final StockEventPublisher stockEventPublisher;

    public ProductSagaEventListener(ProductRepository productRepository,
                                    StockEventPublisher stockEventPublisher) {
        this.productRepository = productRepository;
        this.stockEventPublisher = stockEventPublisher;
    }

    @Transactional
    @RabbitListener(queues = RabbitMqConstants.PRODUCT_ORDER_CREATED_QUEUE)
    public void handleOrderCreated(OrderCreatedEvent event) {

        try {
            reserveStock(event);

            stockEventPublisher.publishStockReserved(
                    new StockReservedEvent(
                            event.orderId(),
                            event.userId(),
                            event.totalPrice(),
                            event.items()
                    )
            );

        } catch (RuntimeException exception) {

            stockEventPublisher.publishStockReservationFailed(
                    new StockReservationFailedEvent(
                            event.orderId(),
                            event.userId(),
                            exception.getMessage()
                    )
            );
        }
    }

    @Transactional
    @RabbitListener(queues = RabbitMqConstants.PRODUCT_PAYMENT_FAILED_QUEUE)
    public void handlePaymentFailed(PaymentFailedEvent event) {

        if (event.items() == null || event.items().isEmpty()) {
            return;
        }

        for (OrderItemEvent item : event.items()) {

            Product product = productRepository.findById(item.productId())
                    .orElse(null);

            if (product == null) {
                continue;
            }

            product.setStock(product.getStock() + item.quantity());
            productRepository.save(product);
        }
    }

    private void reserveStock(OrderCreatedEvent event) {

        List<OrderItemEvent> items = event.items();

        for (OrderItemEvent item : items) {

            Product product = productRepository.findById(item.productId())
                    .orElseThrow(() -> new IllegalStateException("Product not found: " + item.productId()));

            if (!Boolean.TRUE.equals(product.getActive())) {
                throw new IllegalStateException("Product inactive: " + item.productId());
            }

            if (product.getStock() < item.quantity()) {
                throw new IllegalStateException("Insufficient stock: " + item.productId());
            }

            product.setStock(product.getStock() - item.quantity());
            productRepository.save(product);
        }
    }

    @Transactional
    @RabbitListener(queues = RabbitMqConstants.PRODUCT_ORDER_EXPIRED_QUEUE)
    public void handleOrderExpired(OrderExpiredEvent event) {

        for (OrderItemEvent item : event.items()) {

            Product product = productRepository.findById(item.productId())
                    .orElse(null);

            if (product == null) {
                continue;
            }

            product.setStock(product.getStock() + item.quantity());
            productRepository.save(product);
        }
    }

    @Transactional
    @RabbitListener(queues = RabbitMqConstants.PRODUCT_ORDER_CANCELLED_QUEUE)
    public void handleOrderCancelled(OrderCancelledEvent event) {

        for (OrderItemEvent item : event.items()) {

            Product product = productRepository.findById(item.productId())
                    .orElse(null);

            if (product == null) {
                continue;
            }

            product.setStock(product.getStock() + item.quantity());
            productRepository.save(product);
        }
    }



}
