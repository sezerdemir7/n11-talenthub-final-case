package com.demir.ecommerce.productservice.util;

import com.demir.ecommerce.commonlib.excepption.BusinessException;
import com.demir.ecommerce.productservice.exception.message.ProductErrorMessage;

import java.text.Normalizer;
import java.util.Locale;

public final class SlugUtil {

    private SlugUtil() {
    }

    public static String toSlug(String input) {
        if (input == null || input.isBlank()) {
            throw new BusinessException(ProductErrorMessage.SLUG_INPUT_EMPTY);
        }

        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);

        return normalized
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ENGLISH)
                .replace("ı", "i")
                .replaceAll("[^a-z0-9\\s-]", "")
                .trim()
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-");
    }
}