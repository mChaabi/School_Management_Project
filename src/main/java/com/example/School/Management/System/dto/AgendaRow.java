package com.example.School.Management.System.dto;

import java.time.LocalDate;

public record AgendaRow(Long examId, Long courseId, String courseTitle, String teacherName,
                        String title, LocalDate date, Double maxScore, Integer weight,
                        long graded, long total) {}