package com.demir.ecommerce.paymentservice.strategy;

import com.demir.ecommerce.paymentservice.entity.PaymentProvider;
import org.springframework.stereotype.Component;

@Component
public class PaymentStrategyFactory {

    private final MockPaymentStrategy mockPaymentStrategy;
    private final IyzicoPaymentStrategy iyzicoPaymentStrategy;


    public PaymentStrategyFactory(MockPaymentStrategy mockPaymentStrategy,
                                  IyzicoPaymentStrategy iyzicoPaymentStrategy
                                  ) {
        this.mockPaymentStrategy = mockPaymentStrategy;
        this.iyzicoPaymentStrategy = iyzicoPaymentStrategy;

    }

    public PaymentStrategy get(PaymentProvider provider) {
        return switch (provider) {
            case MOCK -> mockPaymentStrategy;
            case IYZICO -> iyzicoPaymentStrategy;
        };
    }
}
