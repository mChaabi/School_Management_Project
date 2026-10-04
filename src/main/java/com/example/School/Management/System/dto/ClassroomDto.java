package com.example.School.Management.System.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ClassroomDto(
        Long id,

        @NotBlank(message = "Classroom name is mandatory")
        @Size(max = 50, message = "Classroom name must not exceed 50 characters")
        String name,

        @NotNull(message = "Capacity is mandatory")
        @Min(value = 1, message = "Capacity must be at least 1")
        Integer capacity,

        // Opcional: Lista de IDs de los estudiantes o dejarlo fuera según prefieras al crear/actualizar
        List<Long> studentIds
) {}
