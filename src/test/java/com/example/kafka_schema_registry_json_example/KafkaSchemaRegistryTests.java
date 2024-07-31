package com.example.kafka_schema_registry_json_example;

import com.example.kafka_schema_registry_json_example.dto.UserDto;
import io.confluent.kafka.schemaregistry.ParsedSchema;
import io.confluent.kafka.schemaregistry.SchemaProvider;
import io.confluent.kafka.schemaregistry.client.MockSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import io.confluent.kafka.schemaregistry.json.JsonSchemaProvider;
import io.confluent.kafka.serializers.json.KafkaJsonSchemaSerializer;
import lombok.SneakyThrows;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.test.context.EmbeddedKafka;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

@SpringBootTest
@EmbeddedKafka(partitions = 1, brokerProperties = {"listeners=PLAINTEXT://localhost:8092", "port=8092"})
class KafkaSchemaRegistryTests {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private SchemaRegistryClient schemaRegistryClient;

    @Test
    @SneakyThrows
    void shouldPassSchema() {
        var schema = Files.readAllLines(Path.of(this.getClass().getClassLoader().getResource("schemas/user.json").toURI())).stream().collect(Collectors.joining("\n"));
        // parse and register the schema
        Optional<ParsedSchema> parsedSchema = schemaRegistryClient.parseSchema("JSON", schema, Collections.EMPTY_LIST);
        Assertions.assertTrue(parsedSchema.isPresent());
        schemaRegistryClient.register("test_user-value", parsedSchema.get(), 1, 1);

        var user = UserDto.builder()
                .email("test@gmail.com")
                .id(1)
                .name("test")
                .build();
        Assertions.assertDoesNotThrow(() -> kafkaTemplate.send("test_user", user));
    }

    @Test
    @SneakyThrows
    void validSchemaWithoutRequiredFieldShouldFail() {
        var schema = Files.readAllLines(Path.of(this.getClass().getClassLoader().getResource("schemas/user.json").toURI())).stream().collect(Collectors.joining("\n"));
        // parse and register the schema
        Optional<ParsedSchema> parsedSchema = schemaRegistryClient.parseSchema("JSON", schema, Collections.EMPTY_LIST);
        Assertions.assertTrue(parsedSchema.isPresent());
        schemaRegistryClient.register("test_user-value", parsedSchema.get(), 1, 1);

        var user = UserDto.builder()
                .id(1)
                .name("test")
                .build();
        Assertions.assertThrows(SerializationException.class, () -> kafkaTemplate.send("test_user", user));
    }

    @Test
    @SneakyThrows
    void wrongSchemaShouldNotPass() {
        var schema = Files.readAllLines(Path.of(this.getClass().getClassLoader().getResource("schemas/wrong-user-schema.json").toURI())).stream().collect(Collectors.joining("\n"));
        // parse and register the schema
        Optional<ParsedSchema> parsedSchema = schemaRegistryClient.parseSchema("JSON", schema, Collections.EMPTY_LIST);
        Assertions.assertTrue(parsedSchema.isPresent());
        schemaRegistryClient.register("test_user-wrong-value", parsedSchema.get(), 1, 1);

        var user = UserDto.builder()
                .email("test@gmail.com")
                .id(1)
                .name("test")
                .build();
        Assertions.assertThrows(SerializationException.class, () -> kafkaTemplate.send("test_user-wrong", user));
    }

    @TestConfiguration
    public static class TestConfig<K, V> {
        @Autowired
        KafkaProperties properties;

        @Bean
        @SneakyThrows
        public SchemaRegistryClient schemaRegistryClient() {
            SchemaProvider provider = new JsonSchemaProvider();
            return new MockSchemaRegistryClient(Collections.singletonList(provider));
        }

        @Bean
        @Primary
        public KafkaJsonSchemaSerializer<V> kafkaJsonSchemaSerializer()
                throws RestClientException, IOException {
            return new KafkaJsonSchemaSerializer<V>(schemaRegistryClient());
        }

        @Bean
        public ProducerFactory<K, V> producerFactory(Serializer<V> vSerializer) {
            return new DefaultKafkaProducerFactory<K, V>(properties.buildProducerProperties(null), (Serializer<K>) new StringSerializer(),
                    vSerializer);
        }
    }
}
