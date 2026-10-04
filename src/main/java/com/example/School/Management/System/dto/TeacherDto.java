package com.example.School.Management.System.dto;

import com.example.School.Management.System.enums.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TeacherDto(
        Long id,

        @NotBlank(message = "First name is mandatory")
        @Size(max = 50)
        String firstName,

        @NotBlank(message = "Last name is mandatory")
        @Size(max = 50)
        String lastName,

        @NotBlank(message = "Email is mandatory")
        @Email(message = "Invalid email format")
        String email,

        String phone,

        String specialization,

        @NotNull(message = "Gender is mandatory")
        Gender gender
) {}