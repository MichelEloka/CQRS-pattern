package com.eloka.cqrs.sync.service;

import com.eloka.cqrs.common.events.ProductEvent;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class SyncStateService {

    private final AtomicLong processedMessages = new AtomicLong();
    private volatile Map<String, Object> lastEvent = Map.of();

    public void markProcessed(ProductEvent event, int partition, long offset) {
        processedMessages.incrementAndGet();
        lastEvent = Map.of(
                "eventId", event.eventId(),
                "eventType", event.eventType(),
                "productId", event.product().id(),
                "partition", partition,
                "offset", offset,
                "emittedAt", event.emittedAt()
        );
    }

    public long processedMessages() {
        return processedMessages.get();
    }

    public Map<String, Object> lastEvent() {
        return lastEvent;
    }
}

