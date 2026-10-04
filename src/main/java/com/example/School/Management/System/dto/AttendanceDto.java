package com.example.School.Management.System.dto;

import com.example.School.Management.System.enums.AttendanceStatus;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record AttendanceDto(
        Long id,

        @NotNull(message = "Student is mandatory")
        Long studentId,

        @NotNull(message = "Date is mandatory")
        LocalDate date,

        @NotNull(message = "Status is mandatory")
        AttendanceStatus status,

        String remark
) {}
