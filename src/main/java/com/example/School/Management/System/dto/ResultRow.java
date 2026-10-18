package com.example.School.Management.System.dto;

import java.time.LocalDate;

public record ResultRow(String courseName, String teacherName, String studentName,
                        String examTitle, LocalDate examDate, boolean absent,
                        Double score, Double maxScore) {}