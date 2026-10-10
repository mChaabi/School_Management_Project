package com.example.School.Management.System.dto;

import java.time.LocalTime;

public record ScheduleCell(Long id, String courseTitle, String classroomName,
                           String teacherName, LocalTime start, LocalTime end) {}
