package com.example.School.Management.System.dto;

import java.time.LocalDate;

public record ExamHeader(Long id, Long courseId, String courseTitle, String title,
                         LocalDate date, Double maxScore, Integer weight) {}