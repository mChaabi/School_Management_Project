package com.example.School.Management.System.dto;


import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record EnrollmentDto(
        Long id,

        @NotNull(message = "Student is mandatory")
        Long studentId,

        @NotNull(message = "Course is mandatory")
        Long courseId,

        LocalDate enrollmentDate,

        Double grade
) {}
