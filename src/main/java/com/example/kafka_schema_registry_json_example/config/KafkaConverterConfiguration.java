package com.example.kafka_schema_registry_json_example.config;

import com.example.kafka_schema_registry_json_example.converter.CustomStringJsonMessageConverter;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.RecordDeserializationException;
import org.apache.kafka.common.errors.SerializationException;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.converter.RecordMessageConverter;

import java.util.HashMap;

@Configuration
@ConditionalOnProperty(name = "kafka.enabled", havingValue = "true", matchIfMissing = true)
public class KafkaConverterConfiguration {

    @Bean
    RecordMessageConverter converter(final ObjectMapper objectMapper) {
        return new CustomStringJsonMessageConverter(objectMapper);
    }

    @Bean
    @Primary
    @SneakyThrows
    CommonErrorHandler errorHandler(final KafkaTemplate<String, Object> kafkaTemplate) {
        //current duplicate of kafka template need to guarantee that the value serializer is a json serializer`
        var config = new HashMap<>(kafkaTemplate.getProducerFactory().getConfigurationProperties());
        config.put("value.serializer", "org.springframework.kafka.support.serializer.JsonSerializer");
        ProducerFactory<String, Object> copyProducerFactory = new DefaultKafkaProducerFactory<>(config);
        KafkaTemplate<String, Object> copyTemplate = new KafkaTemplate<>(copyProducerFactory);
        BeanUtils.copyProperties(kafkaTemplate, copyTemplate);
        //default dlt error handler
        var handler = new DefaultErrorHandler(new DeadLetterPublishingRecoverer(copyTemplate, (record, exception) -> new TopicPartition(record.topic() + ".DLT", 0)));
        handler.addNotRetryableExceptions(SerializationException.class, RecordDeserializationException.class);
        handler.setLogLevel(KafkaException.Level.ERROR);
        return handler;
    }
}
