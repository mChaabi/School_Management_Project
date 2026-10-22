package com.example.School.Management.System.service;

import com.example.School.Management.System.dto.CalendarEventDto;
import com.example.School.Management.System.dto.SchoolEventForm;
import com.example.School.Management.System.entity.Exam;
import com.example.School.Management.System.entity.Schedule;
import com.example.School.Management.System.entity.SchoolEvent;
import com.example.School.Management.System.entity.Student;
import com.example.School.Management.System.enums.EventType;
import com.example.School.Management.System.exception.ResourceNotFoundException;
import com.example.School.Management.System.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CalendarService {

    private static final String CLASS_COLOR   = "#4f46e5";
    private static final String EXAM_COLOR    = "#dc2626";
    private static final String HOLIDAY_COLOR = "#16a34a";
    private static final String EVENT_COLOR   = "#f59e0b";

    private final ExamRepository examRepo;
    private final ScheduleRepository scheduleRepo;
    private final SchoolEventRepository eventRepo;
    private final StudentRepository studentRepo;
    private final EnrollmentRepository enrollmentRepo;

    @Transactional(readOnly = true)
    public List<CalendarEventDto> getEvents(LocalDate from, LocalDate to, Authentication auth) {
        List<CalendarEventDto> out = new ArrayList<>(schoolEvents(from, to));

        boolean staff = hasRole(auth, "ROLE_ADMIN") || hasRole(auth, "ROLE_TEACHER");

        List<Exam> exams;
        List<Schedule> schedules;

        if (staff) {
            exams = examRepo.findInRange(from, to);
            schedules = scheduleRepo.findAllWithDetails();
        } else {
            List<Student> students = myStudents(auth);
            Set<Long> studentIds = students.stream().map(Student::getId).collect(Collectors.toSet());
            Set<Long> classroomIds = students.stream()
                    .filter(s -> s.getClassroom() != null)
                    .map(s -> s.getClassroom().getId())
                    .collect(Collectors.toSet());
            List<Long> courseIds = studentIds.isEmpty()
                    ? List.of() : enrollmentRepo.findCourseIdsByStudentIds(studentIds);

            exams = courseIds.isEmpty() ? List.of() : examRepo.findInRangeForCourses(from, to, courseIds);
            schedules = classroomIds.isEmpty() ? List.of() : scheduleRepo.findByClassroomIds(classroomIds);
        }

        exams.forEach(e -> out.add(toExam(e)));
        schedules.forEach(s -> out.add(toClass(s)));
        return out;
    }

    // ---------- festivos y eventos ----------
    private List<CalendarEventDto> schoolEvents(LocalDate from, LocalDate to) {
        return eventRepo.findByStartDateLessThanEqualAndEndDateGreaterThanEqual(to, from).stream()
                .map(e -> {
                    String color = switch (e.getType()) {
                        case HOLIDAY -> HOLIDAY_COLOR;
                        case EXAM -> EXAM_COLOR;
                        default -> EVENT_COLOR;
                    };
                    return new CalendarEventDto(
                            "event-" + e.getId(), e.getTitle(),
                            e.getStartDate().toString(),
                            e.getEndDate().plusDays(1).toString(),
                            true, null, null, null,
                            color,
                            props(e.getType().name() == "EXAM" ? "EXAM" : e.getType().name(), e.getDescription(), e.getId())
                    );
                })
                .toList();
    }

    @Transactional
    public SchoolEvent createEvent(SchoolEventForm f) {          // antes: void
        LocalDate end = f.endDate() != null ? f.endDate() : f.startDate();
        if (end.isBefore(f.startDate())) throw new IllegalArgumentException("End date before start date");
        return eventRepo.save(SchoolEvent.builder()
                .title(f.title().trim()).description(f.description())
                .type(f.type()).startDate(f.startDate()).endDate(end).build());
    }

    @Transactional
    public void deleteEvent(Long id) {
        if (!eventRepo.existsById(id)) throw new ResourceNotFoundException("Event not found: " + id);
        eventRepo.deleteById(id);
    }

    // ---------- exámenes y clases ----------
    private CalendarEventDto toExam(Exam e) {
        return new CalendarEventDto(
                "exam-" + e.getId(),
                e.getCourse().getTitle() + ": " + e.getTitle(),
                e.getExamDate().toString(), null, true, null, null, null,
                EXAM_COLOR,
                props("EXAM", e.getMaxScore() + " pts · " + e.getWeight() + "%", null));
    }

    private CalendarEventDto toClass(Schedule s) {
        return new CalendarEventDto(
                "class-" + s.getId(),
                s.getCourse().getTitle() + " · " + s.getClassroom().getName(),
                null, null, null,
                List.of(s.getDayOfWeek().getValue() % 7),   // Java: lun=1..dom=7 | FullCalendar: dom=0..sáb=6
                s.getStartTime().toString(), s.getEndTime().toString(),
                CLASS_COLOR, props("CLASS", null, null));
    }

    // ---------- utilidades ----------
    private Map<String, Object> props(String kind, String detail, Long dbId) {
        Map<String, Object> m = new HashMap<>();
        m.put("kind", kind);
        if (detail != null && !detail.isBlank()) m.put("detail", detail);
        if (dbId != null) m.put("dbId", dbId);
        return m;
    }

    private List<Student> myStudents(Authentication auth) {
        if (hasRole(auth, "ROLE_PARENT")) {
            return studentRepo.findByParentUsername(auth.getName());
        }
        if (hasRole(auth, "ROLE_STUDENT")) {
            return studentRepo.findByUserUsername(auth.getName())
                    .map(List::of)
                    .orElseGet(List::of);
        }
        return List.of();
    }

    private boolean hasRole(Authentication auth, String role) {
        return auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals(role));
    }
}
