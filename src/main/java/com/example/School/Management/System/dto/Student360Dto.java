package com.example.School.Management.System.dto;

import java.time.LocalDate;
import java.util.List;

public record Student360Dto(
        Long id, String fullName, String email, String phone,
        String city, String country, String gender, LocalDate birthDate,
        String classroomName, Integer classroomCapacity,
        Double average,
        List<CourseRow> courses,
        List<ExamRow> exams,
        AttendanceSummary attendance,
        List<ScheduleRow> schedule,
        AccountInfo parent,            // NUEVO
        AccountInfo studentAccount
) {
}
