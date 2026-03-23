package com.eloka.cqrs.common.events;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ProductEvent(
        UUID eventId,
        EventType eventType,
        OffsetDateTime emittedAt,
        ProductSnapshot product
) {
}
