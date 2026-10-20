package com.example.School.Management.System.dto;

import java.time.LocalTime;

public record ScheduleRow(String day, LocalTime start, LocalTime end,
                          String courseTitle, String room) {}
