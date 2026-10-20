package com.example.School.Management.System.dto;

import java.time.LocalDate;

public record CourseRow(
        Long id,
        String title,
        String teacherName,
        LocalDate enrollmentDate,
        Double grade
) {}
