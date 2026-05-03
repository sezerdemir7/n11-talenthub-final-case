package com.demir.ecommerce.productservice.unit.messaging;

import com.demir.ecommerce.commonlib.event.seller.SellerActivatedEvent;
import com.demir.ecommerce.commonlib.event.seller.SellerSuspendedEvent;
import com.demir.ecommerce.productservice.messaging.SellerEventListener;
import com.demir.ecommerce.productservice.service.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("SellerEventListener Unit Tests")
class SellerEventListenerTest {

    @Mock
    private ProductService productService;

    @InjectMocks
    private SellerEventListener sellerEventListener;

    private static final Long SELLER_ID = 5L;

    @Nested
    @DisplayName("handleSellerSuspended()")
    class HandleSellerSuspended {

        @Test
        @DisplayName("Should deactivate products by seller id")
        void handleSellerSuspended_validEvent_deactivatesProducts() {
            sellerEventListener.handleSellerSuspended(new SellerSuspendedEvent(SELLER_ID));

            verify(productService).deactivateProductsBySellerId(SELLER_ID);
        }
    }

    @Nested
    @DisplayName("handleSellerActivated()")
    class HandleSellerActivated {

        @Test
        @DisplayName("Should activate products by seller id")
        void handleSellerActivated_validEvent_activatesProducts() {
            sellerEventListener.handleSellerActivated(new SellerActivatedEvent(SELLER_ID));

            verify(productService).activateProductsBySellerId(SELLER_ID);
        }
    }
}
