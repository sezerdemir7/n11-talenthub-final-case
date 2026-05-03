package com.demir.ecommerce.productservice.integration.messaging;

import com.demir.ecommerce.commonlib.event.seller.SellerActivatedEvent;
import com.demir.ecommerce.commonlib.event.seller.SellerSuspendedEvent;
import com.demir.ecommerce.productservice.messaging.SellerEventListener;
import com.demir.ecommerce.productservice.service.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.Mockito.verify;

@SpringBootTest(classes = SellerEventListener.class)
@DisplayName("SellerEventListener Integration Tests")
class SellerEventListenerIntegrationTest {

    @MockitoBean
    private ProductService productService;

    @Autowired
    private SellerEventListener sellerEventListener;

    private static final Long SELLER_ID = 5L;


    @Nested
    @DisplayName("handleSellerSuspended()")
    class HandleSellerSuspended {

        @Test
        @DisplayName("Should call product service to deactivate seller products")
        void handleSellerSuspended_validEvent_callsProductService() {
            sellerEventListener.handleSellerSuspended(new SellerSuspendedEvent(SELLER_ID));

            verify(productService).deactivateProductsBySellerId(SELLER_ID);
        }
    }

    @Nested
    @DisplayName("handleSellerActivated()")
    class HandleSellerActivated {

        @Test
        @DisplayName("Should call product service to activate seller products")
        void handleSellerActivated_validEvent_callsProductService() {
            sellerEventListener.handleSellerActivated(new SellerActivatedEvent(SELLER_ID));

            verify(productService).activateProductsBySellerId(SELLER_ID);
        }
    }
}
