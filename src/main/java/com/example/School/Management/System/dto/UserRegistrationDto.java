package com.example.School.Management.System.dto;

import com.example.School.Management.System.enums.Role;
import jakarta.validation.constraints.NotBlank;
import java.util.Set;

public record UserRegistrationDto(
        @NotBlank(message = "Username is required")
        String username,

        @NotBlank(message = "Password is required")
        String password,

        String email,
        Set<Role> roles
) {
}