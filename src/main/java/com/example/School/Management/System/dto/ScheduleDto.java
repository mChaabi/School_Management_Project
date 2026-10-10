package com.example.School.Management.System.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Data
public class ScheduleDto {
    private Long id;

    @NotNull(message = "Choose a course")
    private Long courseId;

    @NotNull(message = "Choose a classroom")
    private Long classroomId;

    @NotNull(message = "Choose a day")
    private DayOfWeek dayOfWeek;

    @NotNull(message = "Start time is required")
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime endTime;
}
