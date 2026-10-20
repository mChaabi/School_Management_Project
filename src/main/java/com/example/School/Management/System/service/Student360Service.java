package com.example.School.Management.System.service;

import com.example.School.Management.System.dto.*;
import com.example.School.Management.System.entity.Student;
import com.example.School.Management.System.entity.User;
import com.example.School.Management.System.enums.AttendanceStatus;
import com.example.School.Management.System.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class Student360Service {
    private final StudentRepository studentRepo;
    private final EnrollmentRepository enrollmentRepo;
    private final ExamResultRepository examResultRepo;
    private final AttendanceRepository attendanceRepo;
    private final ScheduleRepository scheduleRepo;

    @Transactional(readOnly = true)
    public Page<Student> search(String keyword, Pageable pageable) {
        return studentRepo.searchAll(keyword == null ? "" : keyword.trim(), pageable);
    }

    @Transactional(readOnly = true)
    public List<StudentSuggestDto> suggest(String q) {
        if (q == null || q.trim().length() < 2) return List.of();
        return studentRepo.searchAll(q.trim(), PageRequest.of(0, 8))
                .getContent().stream()
                .map(s -> new StudentSuggestDto(
                        s.getId(),
                        s.getFirstName() + " " + s.getLastName(),
                        s.getEmail(),
                        s.getClassroom() != null ? s.getClassroom().getName() : null))
                .toList();
    }

    @Transactional(readOnly = true)
    public Student360Dto getProfile(Long id) {
        Student s = studentRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Student not found"));

        // 1. Courses (Null-safe mapping)
        var courses = enrollmentRepo.findByStudentIdWithCourse(id).stream()
                .map(e -> new CourseRow(
                        e.getId(),
                        e.getCourse() != null ? e.getCourse().getTitle() : "N/A",
                        e.getCourse() != null && e.getCourse().getTeacher() != null ?
                                e.getCourse().getTeacher().getFirstName() + " " + e.getCourse().getTeacher().getLastName() : "N/A",
                        e.getEnrollmentDate(),
                        e.getGrade()
                ))
                .toList();

        // 2. Exams (Null-safe mapping)
        var exams = examResultRepo.findByStudentId(id).stream()
                .map(r -> new ExamRow(
                        r.getExam() != null ? r.getExam().getTitle() : "N/A",
                        r.getExam() != null && r.getExam().getCourse() != null ? r.getExam().getCourse().getTitle() : "N/A",
                        r.getExam() != null ? r.getExam().getExamDate() : null,
                        r.getScore(),
                        Boolean.TRUE.equals(r.isAbsent()) ? "ABSENT" : "GRADED"
                ))
                .toList();

        // 3. Attendance Summary (Prevent null attendance repo calls)
        var attendance = new AttendanceSummary(
                attendanceRepo.countByStudentIdAndStatus(id, AttendanceStatus.PRESENT),
                attendanceRepo.countByStudentIdAndStatus(id, AttendanceStatus.ABSENT),
                attendanceRepo.countByStudentIdAndStatus(id, AttendanceStatus.LATE)
        );

        // 4. Schedule (Null-safe check for classroom, course, and room)
        var schedule = (s.getClassroom() == null) ? List.<ScheduleRow>of()
                : scheduleRepo.findByClassroomId(s.getClassroom().getId()).stream()
                .map(x -> new ScheduleRow(
                        x.getDayOfWeek() != null ? x.getDayOfWeek().toString() : "N/A",
                        x.getStartTime(),
                        x.getEndTime(),
                        x.getCourse() != null ? x.getCourse().getTitle() : "N/A",
                        x.getClassroom() != null ? x.getClassroom().getName() : "N/A"
                ))
                .toList();

        // 5. Calculate Average
        Double avg = courses.stream()
                .map(CourseRow::grade)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(Double.NaN);

        String averageGradeStr = Double.isNaN(avg) ? null : String.format("%.2f", avg);

        // 6. Build and Return DTO
        return new Student360Dto(s.getId(), s.getFirstName() + " " + s.getLastName(),
                s.getEmail(), s.getPhone(), s.getCity(), s.getCountry(),
                s.getGender() != null ? s.getGender().name() : null, s.getBirthDate(),
                s.getClassroom() != null ? s.getClassroom().getName() : null,
                s.getClassroom() != null ? s.getClassroom().getCapacity() : null,
                avg.isNaN() ? null : avg, courses, exams, attendance, schedule,
                toAccount(s.getParent()),     // NUEVO
                toAccount(s.getUser()));      // NUEVO
    }

    private AccountInfo toAccount(User u) {
        if (u == null) return null;
        return new AccountInfo(
                u.getId(),
                u.getUsername(),
                u.getEmail(),
                u.isEnabled(),
                u.getRoles().stream()
                        .map(r -> r.name().replace("ROLE_", ""))
                        .sorted()
                        .toList());
    }
}
