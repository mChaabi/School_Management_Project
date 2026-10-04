package com.example.School.Management.System.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CourseDto(
        Long id,

        @NotBlank(message = "Course title is mandatory")
        @Size(max = 100)
        String title,

        String description,

        @NotNull(message = "Credits are mandatory")
        @Min(value = 1, message = "Credits must be at least 1")
        Integer credits,

        Long teacherId // Optional: assign a teacher to this course
) {}
