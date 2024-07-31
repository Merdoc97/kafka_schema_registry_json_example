package com.example.kafka_schema_registry_json_example.integration.kafka.producers;

import jakarta.validation.Valid;

public interface KafkaProducer<V> {
    void send(@Valid V message);
}
