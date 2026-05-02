package com.demir.ecommerce.commonlib.messaging;

public final class RabbitMqConstants {

    private RabbitMqConstants() {
    }

    public static final String SAGA_EXCHANGE = "ecommerce.saga.exchange";

    // Queues
    public static final String PRODUCT_ORDER_CREATED_QUEUE = "product.order.created.queue";

    public static final String PAYMENT_STOCK_RESERVED_QUEUE = "payment.stock.reserved.queue";

    public static final String ORDER_PAYMENT_SUCCEEDED_QUEUE = "order.payment.succeeded.queue";
    public static final String ORDER_PAYMENT_FAILED_QUEUE = "order.payment.failed.queue";
    public static final String ORDER_STOCK_RESERVATION_FAILED_QUEUE = "order.stock.reservation.failed.queue";

    public static final String PRODUCT_PAYMENT_FAILED_QUEUE = "product.payment.failed.queue";

    public static final String CART_CLEAR_REQUESTED_QUEUE = "cart.clear.requested.queue";

    // Routing keys
    public static final String ORDER_CREATED_ROUTING_KEY = "order.created";
    public static final String STOCK_RESERVED_ROUTING_KEY = "stock.reserved";
    public static final String STOCK_RESERVATION_FAILED_ROUTING_KEY = "stock.reservation.failed";
    public static final String PAYMENT_SUCCEEDED_ROUTING_KEY = "payment.succeeded";
    public static final String PAYMENT_FAILED_ROUTING_KEY = "payment.failed";
    public static final String CART_CLEAR_REQUESTED_ROUTING_KEY = "cart.clear.requested";

    public static final String PRODUCT_SELLER_SUSPENDED_QUEUE = "product.seller.suspended.queue";

    public static final String SELLER_SUSPENDED_ROUTING_KEY = "seller.suspended";

    public static final String PRODUCT_ORDER_EXPIRED_QUEUE = "product.order.expired.queue";
    public static final String ORDER_EXPIRED_ROUTING_KEY = "order.expired";

    public static final String PRODUCT_ORDER_CANCELLED_QUEUE = "product.order.cancelled.queue";
    public static final String PAYMENT_ORDER_CANCELLED_QUEUE = "payment.order.cancelled.queue";

    public static final String ORDER_CANCELLED_ROUTING_KEY = "order.cancelled";

    public static final String PRODUCT_SELLER_ACTIVATED_QUEUE = "product.seller.activated.queue";

    public static final String SELLER_ACTIVATED_ROUTING_KEY = "seller.activated";


    public static final String NOTIFICATION_ORDER_CREATED_QUEUE = "notification.order.created.queue";
    public static final String NOTIFICATION_ORDER_CANCELLED_QUEUE = "notification.order.cancelled.queue";



}
