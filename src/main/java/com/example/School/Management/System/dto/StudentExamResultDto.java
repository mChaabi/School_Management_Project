package com.example.School.Management.System.dto;

public record StudentExamResultDto(
        Long examResultId,
        String courseName,
        String teacherName,
        String studentName,
        String examTitle,
        Double score,
        Double maxScore,
        boolean absent
) {}