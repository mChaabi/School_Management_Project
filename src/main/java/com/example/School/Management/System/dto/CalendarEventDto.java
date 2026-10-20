package com.example.School.Management.System.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CalendarEventDto(
        String id,
        String title,
        String start,                    // "2026-10-25"
        String end,
        Boolean allDay,
        List<Integer> daysOfWeek,        // clases repetidas cada semana
        String startTime,                // "10:00"
        String endTime,
        String color,
        Map<String, Object> extendedProps
) {}
