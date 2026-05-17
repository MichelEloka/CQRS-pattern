package com.eloka.cqrs.write.service;

import com.eloka.cqrs.common.domain.DeploymentRegion;
import com.eloka.cqrs.common.events.EventType;
import com.eloka.cqrs.common.events.ProductEvent;
import com.eloka.cqrs.common.events.ProductSnapshot;
import com.eloka.cqrs.write.dto.PatchProductRequest;
import com.eloka.cqrs.write.dto.UpsertProductRequest;
import com.eloka.cqrs.write.repository.ProductWriteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class ProductCommandService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductCommandService.class);

    private final ProductWriteRepository productWriteRepository;
    private final KafkaTemplate<String, ProductEvent> kafkaTemplate;
    private final String topicName;
    private final DeploymentRegion deploymentRegion;

    public ProductCommandService(
            ProductWriteRepository productWriteRepository,
            KafkaTemplate<String, ProductEvent> kafkaTemplate,
            @Value("${cqrs.kafka.topic}") String topicName,
            @Value("${cqrs.region}") String regionCode
    ) {
        this.productWriteRepository = productWriteRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.topicName = topicName;
        this.deploymentRegion = DeploymentRegion.fromCode(regionCode);
    }

    @Transactional
    public ProductSnapshot create(UpsertProductRequest request) {
        ProductSnapshot product = productWriteRepository.insert(UUID.randomUUID(), deploymentRegion.code(), request);
        LOGGER.info("Write model persisted product {} in region {}", product.id(), deploymentRegion.code());
        publish(EventType.PRODUCT_CREATED, product);
        return product;
    }

    @Transactional
    public Optional<ProductSnapshot> replace(UUID productId, UpsertProductRequest request) {
        Optional<ProductSnapshot> product = productWriteRepository.replace(productId, deploymentRegion.code(), request);
        product.ifPresentOrElse(snapshot -> {
            LOGGER.info("Write model replaced product {} in region {}", snapshot.id(), deploymentRegion.code());
            publish(EventType.PRODUCT_UPDATED, snapshot);
        }, () -> LOGGER.warn("Write model could not replace product {} in region {}", productId, deploymentRegion.code()));
        return product;
    }

    @Transactional
    public Optional<ProductSnapshot> patch(UUID productId, PatchProductRequest request) {
        if (request.isEmpty()) {
            throw new IllegalArgumentException("PATCH requiert au moins un champ modifiable.");
        }

        Optional<ProductSnapshot> product = productWriteRepository.patch(productId, deploymentRegion.code(), request);
        product.ifPresentOrElse(snapshot -> {
            LOGGER.info("Write model patched product {} in region {}", snapshot.id(), deploymentRegion.code());
            publish(EventType.PRODUCT_UPDATED, snapshot);
        }, () -> LOGGER.warn("Write model could not patch product {} in region {}", productId, deploymentRegion.code()));
        return product;
    }

    private void publish(EventType eventType, ProductSnapshot product) {
        ProductEvent event = new ProductEvent(
                UUID.randomUUID(),
                eventType,
                OffsetDateTime.now(),
                product
        );

        try {
            kafkaTemplate.send(topicName, deploymentRegion.partition(), deploymentRegion.code(), event).get(10, TimeUnit.SECONDS);
            LOGGER.info(
                    "Published event {} for product {} to topic {} partition {}",
                    event.eventType(),
                    product.id(),
                    topicName,
                    deploymentRegion.partition()
            );
        } catch (Exception exception) {
            throw new IllegalStateException("Publication Kafka impossible.", exception);
        }
    }
}
