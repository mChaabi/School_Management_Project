package com.example.School.Management.System.dto;

import com.example.School.Management.System.enums.Gender;

import java.util.List;

public record TeacherDetailDto(Long id, String fullName, String email, String phone,
                               String specialization, Gender gender,
                               int courseCount, long studentCount, int totalCredits,
                               Double overallAverage, List<TeacherCourseRow> courses) {}
