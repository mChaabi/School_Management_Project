package com.example.School.Management.System.dto;

public record TeacherCourseRow(Long courseId, String title, Integer credits,
                               long studentCount, Double average) {}