package com.eloka.cqrs.sync.service;

import com.eloka.cqrs.common.events.ProductEvent;
import com.eloka.cqrs.sync.repository.ProductProjectionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.TopicPartition;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
public class ProductEventConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductEventConsumer.class);

    private final ProductProjectionRepository productProjectionRepository;
    private final SyncStateService syncStateService;
    private final String configuredRegion;
    private final int configuredPartition;

    public ProductEventConsumer(
            ProductProjectionRepository productProjectionRepository,
            SyncStateService syncStateService,
            @Value("${cqrs.region}") String configuredRegion,
            @Value("${cqrs.kafka.partition}") int configuredPartition
    ) {
        this.productProjectionRepository = productProjectionRepository;
        this.syncStateService = syncStateService;
        this.configuredRegion = configuredRegion;
        this.configuredPartition = configuredPartition;
    }

    @KafkaListener(topicPartitions = @TopicPartition(
            topic = "${cqrs.kafka.topic}",
            partitions = "${cqrs.kafka.partition}"
    ))
    public void onMessage(
            ProductEvent event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset
    ) {
        if (partition != configuredPartition) {
            LOGGER.error(
                    "Ignoring event for product {}: expected partition {}, received partition {}",
                    event.product().id(),
                    configuredPartition,
                    partition
            );
            return;
        }

        if (!configuredRegion.equals(event.product().region())) {
            LOGGER.error(
                    "Ignoring event for product {}: sync-service region {} received event for region {}",
                    event.product().id(),
                    configuredRegion,
                    event.product().region()
            );
            return;
        }

        productProjectionRepository.upsert(event.product());
        syncStateService.markProcessed(event, partition, offset);

        LOGGER.info(
                "Projection updated for product {} in region {} on partition {} with event {}",
                event.product().id(),
                event.product().region(),
                partition,
                event.eventType()
        );
    }
}
