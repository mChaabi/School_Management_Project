package com.example.School.Management.System.dto;

import com.example.School.Management.System.enums.Gender;

import java.time.LocalDate;
import java.util.List;

public record StudentDetailDto(
        Long id,
        String fullName,
        String email,
        String phone,
        Gender gender,
        LocalDate birthDate,
        String classroomName,
        Integer classroomCapacity,
        List<StudentCourseRow> courses,
        Double average) {}