package com.demir.ecommerce.paymentservice.dto;

import jakarta.validation.constraints.NotBlank;

public record PaymentCardDto(

        @NotBlank
        String cardHolderName,

        @NotBlank
        String cardNumber,

        @NotBlank
        String expireMonth,

        @NotBlank
        String expireYear,

        @NotBlank
        String cvc
) {
}
