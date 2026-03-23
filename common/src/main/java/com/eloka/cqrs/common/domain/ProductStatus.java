package com.eloka.cqrs.common.domain;

public enum ProductStatus {
    AVAILABLE,
    LIMITED,
    DISCONTINUED;

    public static ProductStatus fromNullable(String value) {
        if (value == null || value.isBlank()) {
            return AVAILABLE;
        }

        return ProductStatus.valueOf(value.trim().toUpperCase());
    }
}

