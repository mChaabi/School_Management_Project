package com.example.School.Management.System.dto;

public record RiskStudentDto(
        Long id,
        String fullName,
        String classroomName,
        Double average,
        long absences,
        boolean lowAverage,
        boolean manyAbsences
) {}
