package com.example.kafka_schema_registry_json_example.converter;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.utils.Bytes;
import org.springframework.kafka.support.KafkaNull;
import org.springframework.kafka.support.converter.ConversionException;
import org.springframework.kafka.support.converter.JsonMessageConverter;
import org.springframework.kafka.support.mapping.DefaultJackson2JavaTypeMapper;
import org.springframework.kafka.support.mapping.Jackson2JavaTypeMapper;
import org.springframework.util.Assert;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;

/**
 * current implementation of JsonMessageConverter does not support confluent schema registry
 * this implementation is a workaround to support confluent schema registry
 */
public class ConfluentJsonMessageConverter extends JsonMessageConverter {
    private static final JavaType OBJECT = TypeFactory.defaultInstance().constructType(Object.class);
    private final ObjectMapper objectMapper;
    private final Jackson2JavaTypeMapper typeMapper = new DefaultJackson2JavaTypeMapper();

    public ConfluentJsonMessageConverter(ObjectMapper objectMapper) {
        super(objectMapper);
        Assert.notNull(objectMapper, "'objectMapper' must not be null.");
        this.objectMapper = objectMapper;
    }

    @Override
    protected Object extractAndConvertValue(ConsumerRecord<?, ?> record, Type type) {
        Object value = record.value();
        if (record.value() == null) {
            return KafkaNull.INSTANCE;
        }

        JavaType javaType = this.determineJavaType(record, type);
        if (value instanceof Bytes) {
            value = ((Bytes) value).get();
        }
        if (value instanceof String) {
            try {
                //need to trim because KafkaJsonSchemaSerializer add a space at the beginning of the string
                return this.objectMapper.readValue(((String) value).trim(), javaType);
            } catch (IOException e) {
                throw new ConversionException("Failed to convert from JSON", record, e);
            }

        }
        if (value instanceof Map<?, ?> map) {
            try {
                return this.objectMapper.convertValue(map, javaType);
            } catch (Exception e) {
                throw new ConversionException("Failed to convert from Map", record, e);
            }
        } else if (value instanceof byte[]) {
            try {
                return this.objectMapper.readValue((byte[]) value, javaType);
            } catch (IOException e) {
                throw new ConversionException("Failed to convert from JSON", record, e);
            }
        } else {
            throw new IllegalStateException("Only String, Bytes, or byte[] supported");
        }
    }

    private JavaType determineJavaType(ConsumerRecord<?, ?> record, Type type) {
        JavaType javaType = this.typeMapper.getTypePrecedence().equals(Jackson2JavaTypeMapper.TypePrecedence.INFERRED) && type != null
                ? TypeFactory.defaultInstance().constructType(type)
                : this.typeMapper.toJavaType(record.headers());
        if (javaType == null) { // no headers
            if (type != null) {
                javaType = TypeFactory.defaultInstance().constructType(type);
            } else {
                javaType = OBJECT;
            }
        }
        return javaType;
    }
}
