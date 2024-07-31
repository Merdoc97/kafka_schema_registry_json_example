package com.example.kafka_schema_registry_json_example.controllers;

import com.example.kafka_schema_registry_json_example.dto.UserDto;
import com.example.kafka_schema_registry_json_example.integration.kafka.producers.KafkaProducer;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@Validated
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final KafkaProducer<UserDto> userProducer;

    @PostMapping
    public ResponseEntity<HttpStatus> sendUser(@Valid @RequestBody UserDto userDto) {
        userProducer.send(userDto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
