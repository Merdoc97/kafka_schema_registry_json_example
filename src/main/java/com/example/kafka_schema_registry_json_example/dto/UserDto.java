package com.example.kafka_schema_registry_json_example.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDto {
    @NotNull
    @Positive
    private Integer id;
    @NotEmpty
    private String name;
    @NotEmpty
    @Email(message = "email should be valid and has email format")
    private String email;
}
