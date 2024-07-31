package com.example.kafka_schema_registry_json_example.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.support.KafkaNull;
import org.springframework.kafka.support.converter.ConversionException;
import org.springframework.messaging.Message;

public class CustomStringJsonMessageConverter extends ConfluentJsonMessageConverter{
    public CustomStringJsonMessageConverter(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Override
    protected Object convertPayload(Message<?> message) {
        try {
            return message.getPayload() instanceof KafkaNull
                    ? null
                    : getObjectMapper().writeValueAsString(message.getPayload());
        }
        catch (JsonProcessingException e) {
            throw new ConversionException("Failed to convert to JSON", message, e);
        }
    }
}
