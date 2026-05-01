package com.demir.ecommerce.productservice.messaging;

import com.demir.ecommerce.commonlib.event.seller.SellerSuspendedEvent;
import com.demir.ecommerce.commonlib.messaging.RabbitMqConstants;
import com.demir.ecommerce.productservice.service.ProductService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class SellerEventListener {

    private final ProductService productService;

    public SellerEventListener(ProductService productService) {
        this.productService = productService;
    }

    @RabbitListener(queues = RabbitMqConstants.PRODUCT_SELLER_SUSPENDED_QUEUE)
    public void handleSellerSuspended(SellerSuspendedEvent event) {
        productService.deactivateProductsBySellerId(event.sellerId());
    }
}
