package com.eloka.cqrs.write.service;

import com.eloka.cqrs.common.events.EventType;
import com.eloka.cqrs.common.events.ProductEvent;
import com.eloka.cqrs.common.events.ProductSnapshot;
import com.eloka.cqrs.write.dto.PatchProductRequest;
import com.eloka.cqrs.write.dto.UpsertProductRequest;
import com.eloka.cqrs.write.repository.ProductWriteRepository;
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

    private final ProductWriteRepository productWriteRepository;
    private final KafkaTemplate<String, ProductEvent> kafkaTemplate;
    private final String topicName;

    public ProductCommandService(
            ProductWriteRepository productWriteRepository,
            KafkaTemplate<String, ProductEvent> kafkaTemplate,
            @Value("${cqrs.kafka.topic}") String topicName
    ) {
        this.productWriteRepository = productWriteRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.topicName = topicName;
    }

    @Transactional
    public ProductSnapshot create(UpsertProductRequest request) {
        ProductSnapshot product = productWriteRepository.insert(UUID.randomUUID(), request);
        publish(EventType.PRODUCT_CREATED, product);
        return product;
    }

    @Transactional
    public Optional<ProductSnapshot> replace(UUID productId, UpsertProductRequest request) {
        Optional<ProductSnapshot> product = productWriteRepository.replace(productId, request);
        product.ifPresent(snapshot -> publish(EventType.PRODUCT_UPDATED, snapshot));
        return product;
    }

    @Transactional
    public Optional<ProductSnapshot> patch(UUID productId, PatchProductRequest request) {
        if (request.isEmpty()) {
            throw new IllegalArgumentException("PATCH requiert au moins un champ modifiable.");
        }

        Optional<ProductSnapshot> product = productWriteRepository.patch(productId, request);
        product.ifPresent(snapshot -> publish(EventType.PRODUCT_UPDATED, snapshot));
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
            kafkaTemplate.send(topicName, product.id().toString(), event).get(10, TimeUnit.SECONDS);
        } catch (Exception exception) {
            throw new IllegalStateException("Publication Kafka impossible.", exception);
        }
    }
}

