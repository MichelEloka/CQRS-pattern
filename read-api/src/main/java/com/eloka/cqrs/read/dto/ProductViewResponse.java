package com.eloka.cqrs.read.dto;

import com.eloka.cqrs.common.domain.ProductStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ProductViewResponse(
        UUID id,
        String name,
        String description,
        BigDecimal price,
        Integer stock,
        ProductStatus status,
        String availability,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        OffsetDateTime syncedAt
) {
}

