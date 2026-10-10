package com.example.School.Management.System.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.Data;

@Data
public class GradeRow {
    private Long enrollmentId;
    private String studentName;

    @DecimalMin("0.0") @DecimalMax("20.0")
    private Double grade;
}