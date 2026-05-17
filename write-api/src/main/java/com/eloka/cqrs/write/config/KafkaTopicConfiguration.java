package com.eloka.cqrs.write.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

import java.util.List;

@Configuration
public class KafkaTopicConfiguration {

    @Bean
    public NewTopic productEventsTopic(@Value("${cqrs.kafka.topic}") String topicName) {
        return TopicBuilder.name(topicName)
                .partitions(3)
                .replicasAssignments(java.util.Map.of(
                        0, List.of(0, 1, 2),
                        1, List.of(1, 2, 0),
                        2, List.of(2, 0, 1)
                ))
                .config("min.insync.replicas", "2")
                .build();
    }
}
