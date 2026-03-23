package com.eloka.cqrs.common.events;

import com.eloka.cqrs.common.domain.ProductStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ProductSnapshot(
        UUID id,
        String name,
        String description,
        BigDecimal price,
        Integer stock,
        ProductStatus status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}

