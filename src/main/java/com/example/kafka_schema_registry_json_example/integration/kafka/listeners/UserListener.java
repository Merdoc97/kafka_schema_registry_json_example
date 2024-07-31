package com.example.kafka_schema_registry_json_example.integration.kafka.listeners;

import com.example.kafka_schema_registry_json_example.dto.UserDto;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@Validated
@Slf4j
public class UserListener {

    @KafkaListener(topics = "${kafka.consumer.topic}")
    public void processUser(final @Payload @Valid UserDto userDto) {
        log.info("consuming user: {}", userDto);
    }
}
