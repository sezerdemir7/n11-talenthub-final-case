package com.demir.ecommerce.commonlib.logging;

public final class CorrelationConstants {

    private CorrelationConstants() {
    }

    public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
    public static final String CORRELATION_ID_MDC_KEY = "correlationId";
}
