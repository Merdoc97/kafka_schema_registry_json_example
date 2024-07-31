package com.example.kafka_schema_registry_json_example.integration.kafka.producers;

import com.example.kafka_schema_registry_json_example.dto.UserDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@RequiredArgsConstructor
@Validated
@Slf4j
public class UserProducer implements KafkaProducer<UserDto> {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    @Value("${kafka.consumer.topic}")
    private final String topic;

    @Override
    public void send(@Valid @Payload UserDto message) {
        log.info("sending user {} to topic {}", topic, message);
        kafkaTemplate.send(topic, message);
        log.info("message sent to kafka topic: {} with message: {}", topic, message);
    }
}
