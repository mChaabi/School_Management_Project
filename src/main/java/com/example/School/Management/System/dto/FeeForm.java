package com.example.School.Management.System.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FeeForm(
        Long studentId,                 // uno de los dos: estudiante...
        Long classroomId,               // ...o aula completa
        @NotBlank @Size(max = 120) String title,
        @NotNull @DecimalMin("0.01") @Digits(integer = 8, fraction = 2) BigDecimal amount,
        @NotNull LocalDate dueDate
) {}