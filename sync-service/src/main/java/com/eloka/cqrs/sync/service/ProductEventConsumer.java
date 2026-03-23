package com.eloka.cqrs.sync.service;

import com.eloka.cqrs.common.events.ProductEvent;
import com.eloka.cqrs.sync.repository.ProductProjectionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
public class ProductEventConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductEventConsumer.class);

    private final ProductProjectionRepository productProjectionRepository;
    private final SyncStateService syncStateService;

    public ProductEventConsumer(
            ProductProjectionRepository productProjectionRepository,
            SyncStateService syncStateService
    ) {
        this.productProjectionRepository = productProjectionRepository;
        this.syncStateService = syncStateService;
    }

    @KafkaListener(topics = "${cqrs.kafka.topic}")
    public void onMessage(
            ProductEvent event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset
    ) {
        productProjectionRepository.upsert(event.product());
        syncStateService.markProcessed(event, partition, offset);

        LOGGER.info("Projection updated for product {} with event {}", event.product().id(), event.eventType());
    }
}

