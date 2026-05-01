package com.demir.ecommerce.commonlib.logging;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.slf4j.MDC;

public class CorrelationFeignInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        String correlationId = MDC.get(CorrelationConstants.CORRELATION_ID_MDC_KEY);

        if (correlationId != null && !correlationId.isBlank()) {
            template.header(CorrelationConstants.CORRELATION_ID_HEADER, correlationId);
        }
    }
}
