package com.demir.ecommerce.orderservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Delivery address information")
public record AddressDto(

        @Schema(description = "City", example = "Istanbul", requiredMode = Schema.RequiredMode.REQUIRED)
        String city,

        @Schema(description = "District", example = "Kadikoy", requiredMode = Schema.RequiredMode.REQUIRED)
        String district,

        @Schema(description = "Full address", example = "Bagdat Caddesi No:10 Daire 5", requiredMode = Schema.RequiredMode.REQUIRED)
        String fullAddress,

        @Schema(description = "Postal code", example = "34710")
        String postalCode
) {}