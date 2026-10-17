package com.example.School.Management.System.entity;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ExamForm {
    @NotBlank(message = "Exam title is mandatory")
    @Size(max = 100, message = "Title must not exceed 100 characters")
    private String title;

    @NotNull(message = "Exam date is mandatory")
    private LocalDate examDate;

    @NotNull(message = "Maximum score is mandatory")
    @DecimalMin(value = "1", message = "Maximum score must be at least 1")
    @DecimalMax(value = "100", message = "Maximum score must not exceed 100")
    private Double maxScore = 20.0;

    @NotNull(message = "Weight is mandatory")
    @Min(value = 1, message = "Weight must be at least 1")
    @Max(value = 100, message = "Weight must not exceed 100")
    private Integer weight;
}