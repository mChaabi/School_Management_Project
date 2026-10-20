package com.example.School.Management.System.dto;

import java.time.LocalDate;

public record ExamRow(String examTitle, String courseTitle, LocalDate date,
                      Double score, String status) {}