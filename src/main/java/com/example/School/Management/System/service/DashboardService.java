package com.example.School.Management.System.service;

import com.example.School.Management.System.dto.DashboardDto;
import com.example.School.Management.System.dto.DayStatusCount;
import com.example.School.Management.System.dto.LabelCount;
import com.example.School.Management.System.entity.Course;
import com.example.School.Management.System.enums.AttendanceStatus;
import com.example.School.Management.System.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final StudentRepository studentRepo;
    private final TeacherRepository teacherRepo;
    private final CourseRepository courseRepo;
    private final EnrollmentRepository enrollmentRepo;
    private final ClassroomRepository classroomRepo;
    private final AttendanceRepository attendanceRepo;

    @Transactional(readOnly = true)
    public DashboardDto build() {
        LocalDate today = LocalDate.now();

        // Classrooms chart
        var perClass = classroomRepo.countStudentsPerClassroom();
        List<String> classLabels = perClass.stream().map(LabelCount::getLabel).toList();
        List<Long> classCounts = perClass.stream().map(LabelCount::getTotal).toList();

        // Attendance last 7 days
        LocalDate from = today.minusDays(6);
        var rows = attendanceRepo.countByDayAndStatus(from);
        List<String> days = new ArrayList<>();
        List<Long> present = new ArrayList<>(), absent = new ArrayList<>(), late = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate d = from.plusDays(i);
            days.add(d.format(DateTimeFormatter.ofPattern("dd/MM")));
            present.add(sum(rows, d, AttendanceStatus.PRESENT));
            absent.add(sum(rows, d, AttendanceStatus.ABSENT));
            late.add(sum(rows, d, AttendanceStatus.LATE));
        }

        // Attendance rate today
        long total = attendanceRepo.countByDay(today);
        Double rate = total == 0 ? null
                : 100.0 * attendanceRepo.countByDayAndStatus(today, AttendanceStatus.PRESENT) / total;

        // Grades
        List<Double> grades = enrollmentRepo.findAllGrades();
        Double avg = grades.isEmpty() ? null
                : grades.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        long[] b = new long[4];   // <10, 10-13.99, 14-16.99, 17+
        for (double g : grades) b[g < 10 ? 0 : g < 14 ? 1 : g < 17 ? 2 : 3]++;

        return new DashboardDto(
                studentRepo.count(), teacherRepo.count(), courseRepo.count(), enrollmentRepo.count(),
                rate, avg,
                classLabels, classCounts, days, present, absent, late,
                List.of(b[0], b[1], b[2], b[3]),
                courseRepo.findByTeacherIsNull().stream().map(Course::getTitle).toList(),
                studentRepo.findByClassroomIsNull().stream()
                        .map(s -> s.getFirstName() + " " + s.getLastName()).toList(),
                attendanceRepo.findFrequentAbsentees(today.minusDays(30), 3).size());
    }

    private long sum(List<DayStatusCount> rows, LocalDate d, AttendanceStatus st) {
        return rows.stream()
                .filter(r -> r.getLabel().equals(d) && r.getStatus() == st)
                .mapToLong(DayStatusCount::getTotal).sum();
    }
}
