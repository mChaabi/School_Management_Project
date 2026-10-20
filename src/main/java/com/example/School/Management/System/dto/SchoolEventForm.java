package com.example.School.Management.System.dto;

import com.example.School.Management.System.enums.EventType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record SchoolEventForm(
        @NotBlank @Size(max = 120) String title,
        @NotNull EventType type,
        @NotNull LocalDate startDate,
        LocalDate endDate,
        @Size(max = 500) String description
) {}