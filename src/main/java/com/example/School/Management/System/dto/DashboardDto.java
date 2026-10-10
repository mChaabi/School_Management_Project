package com.example.School.Management.System.dto;

import java.util.List;

public record DashboardDto(
        long students, long teachers, long courses, long enrollments,
        Double attendanceRateToday, Double schoolAverage,
        List<String> classroomLabels, List<Long> classroomCounts,
        List<String> days, List<Long> present, List<Long> absent, List<Long> late,
        List<Long> gradeBuckets,
        List<String> coursesWithoutTeacher,
        List<String> studentsWithoutClassroom,
        int frequentAbsentees) {}
