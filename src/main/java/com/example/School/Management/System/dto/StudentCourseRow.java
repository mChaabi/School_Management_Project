package com.example.School.Management.System.dto;

import java.time.LocalDate;

public record StudentCourseRow(
        Long courseId,
        String courseTitle,
        Integer credits,
        String teacherName,
        String specialization,
        LocalDate enrollmentDate,
        Double grade) {}