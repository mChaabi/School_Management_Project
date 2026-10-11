package com.example.School.Management.System.service;

import com.example.School.Management.System.entity.Course;
import com.example.School.Management.System.entity.Notification;
import com.example.School.Management.System.entity.Student;
import com.example.School.Management.System.entity.User;
import com.example.School.Management.System.enums.AttendanceStatus;
import com.example.School.Management.System.enums.NotificationType;
import com.example.School.Management.System.enums.Role;
import com.example.School.Management.System.prompt.PromptTemplates;
import com.example.School.Management.System.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final NotificationRepository repo;
    private final UserRepository userRepo;
    private final StudentRepository studentRepo;
    private final CourseRepository courseRepo;
    private final ScheduleRepository scheduleRepo;
    private final PromptTemplates prompts;
    private final AiClient ai;

    @Value("${app.notifications.language:English}")
    private String language;

    // =====================================================================
    //  CORE: one method that every event goes through
    // =====================================================================
    public int notify(NotificationType type, Map<String, String> facts,
                      Collection<User> recipients, String link, String dedupKey) {

        // 1. unique recipients
        Map<Long, User> unique = new LinkedHashMap<>();
        for (User u : recipients) if (u != null) unique.putIfAbsent(u.getId(), u);

        // 2. skip people who already got this exact event
        if (dedupKey != null) {
            unique.keySet().removeIf(id -> repo.existsByRecipientIdAndDedupKey(id, dedupKey));
        }
        if (unique.isEmpty()) {
            log.info("🔕 [NOTIFY] {} {} - nobody to notify (or already sent)", type.getEmoji(), type);
            return 0;
        }

        // 3. text: AI first, fallback template second (no DB transaction is open during the HTTP call)
        String system = prompts.render(prompts.load("system"), Map.of("language", language));
        String userPrompt = prompts.render(prompts.load(type.promptName()), facts);
        Optional<String> aiText = ai.complete(system, userPrompt).map(this::clean).filter(t -> !t.isBlank());
        String message = aiText.orElseGet(() -> prompts.render(type.getFallback(), facts));

        // 4. save one row per recipient
        List<Notification> rows = unique.keySet().stream()
                .map(id -> Notification.builder()
                        .recipient(userRepo.getReferenceById(id))
                        .type(type).message(message).link(link)
                        .dedupKey(dedupKey).aiGenerated(aiText.isPresent())
                        .seen(false).build())
                .toList();
        repo.saveAll(rows);

        // 5. log with icons
        log.info("🔔 [NOTIFY] {} -> {} recipient(s) | {} | {}",
                type, rows.size(), aiText.isPresent() ? "🤖 AI" : "📝 fallback", type.format(message));
        return rows.size();
    }

    // =====================================================================
    //  EVENTS (called from your other services / controllers in Part B)
    // =====================================================================

    /** Absence or late arrival. Other statuses are ignored. */
    @Async
    public void onAttendance(Long studentId, LocalDate date, AttendanceStatus status, String remark) {
        if (status == null || date == null) return;
        NotificationType type = switch (status) {
            case ABSENT -> NotificationType.ABSENCE;
            case LATE   -> NotificationType.LATE;
            default     -> null;
        };
        if (type == null) return;

        safely(() -> {
            Student s = studentRepo.findForNotification(studentId).orElseThrow();
            notify(type,
                    facts("student", name(s),
                            "classroom", s.getClassroom() != null ? s.getClassroom().getName() : null,
                            "date", date.format(DATE),
                            "lessons", lessonsOn(s, date),
                            "remark", remark),
                    audience(s), "/attendances/student/" + studentId,
                    type.name() + ":" + studentId + ":" + date);
        });
    }

    @Async
    public void onGradeAdded(Long studentId, Long courseId, Double grade) {
        if (grade == null) return;
        safely(() -> {
            Student s = studentRepo.findForNotification(studentId).orElseThrow();
            Course c = courseRepo.findForNotification(courseId).orElseThrow();
            notify(NotificationType.GRADE_ADDED,
                    facts("student", name(s), "course", c.getTitle(),
                            "grade", String.format(Locale.ROOT, "%.2f", grade),
                            "teacher", teacherName(c)),
                    audience(s), "/enrollments",
                    "GRADE:" + studentId + ":" + courseId + ":" + grade);   // a changed grade notifies again
        });
    }

    @Async
    public void onEnrollment(Long studentId, Long courseId) {
        safely(() -> {
            Student s = studentRepo.findForNotification(studentId).orElseThrow();
            Course c = courseRepo.findForNotification(courseId).orElseThrow();
            List<User> to = new ArrayList<>(audience(s));
            if (c.getTeacher() != null) to.add(c.getTeacher().getUser());
            notify(NotificationType.ENROLLMENT_CREATED,
                    facts("student", name(s), "course", c.getTitle(), "teacher", teacherName(c)),
                    to, "/enrollments", "ENROLL:" + studentId + ":" + courseId);
        });
    }

    @Async
    public void onStudentCreated(Long studentId, String fullName, String classroomName) {
        safely(() -> notify(NotificationType.STUDENT_CREATED,
                facts("student", fullName, "classroom", classroomName),
                staff(), "/students/" + studentId, "STUDENT_NEW:" + studentId));
    }

    @Async
    public void onTeacherCreated(Long teacherId, String fullName, String specialization) {
        safely(() -> notify(NotificationType.TEACHER_CREATED,
                facts("teacher", fullName, "specialization", specialization),
                staff(), "/teachers/" + teacherId, "TEACHER_NEW:" + teacherId));
    }

    @Async
    public void onCourseCreated(Long courseId) {
        safely(() -> {
            Course c = courseRepo.findForNotification(courseId).orElseThrow();
            List<User> to = new ArrayList<>(staff());
            if (c.getTeacher() != null) to.add(c.getTeacher().getUser());
            notify(NotificationType.COURSE_CREATED,
                    facts("course", c.getTitle(),
                            "credits", c.getCredits() != null ? c.getCredits().toString() : null,
                            "teacher", teacherName(c)),
                    to, "/courses", "COURSE_NEW:" + courseId);
        });
    }

    /** Call it BEFORE the students are moved or the classroom is removed. */
    @Async
    public void onClassroomClosed(Long classroomId, String classroomName) {
        safely(() -> {
            List<Student> students = studentRepo.findByClassroomForNotification(classroomId);
            List<User> to = new ArrayList<>(staff());
            students.forEach(s -> to.addAll(audience(s)));
            notify(NotificationType.CLASSROOM_CLOSED,
                    facts("classroom", classroomName, "students", String.valueOf(students.size())),
                    to, "/classrooms", "CLASSROOM_CLOSED:" + classroomId);
        });
    }

    /** action = "added" | "updated" | "cancelled" */
    @Async
    public void onScheduleChanged(Long courseId, Long classroomId, String classroomName,
                                  String action, String day, String time) {
        safely(() -> {
            Course c = courseRepo.findForNotification(courseId).orElseThrow();
            List<User> to = new ArrayList<>();
            studentRepo.findByClassroomForNotification(classroomId).forEach(s -> to.addAll(audience(s)));
            if (c.getTeacher() != null) to.add(c.getTeacher().getUser());
            notify(NotificationType.SCHEDULE_CHANGED,
                    facts("action", action, "course", c.getTitle(), "classroom", classroomName,
                            "day", day, "time", time),
                    to, "/schedules",
                    "SCHEDULE:" + courseId + ":" + classroomId + ":" + day + ":" + time + ":" + action);
        });
    }

    // =====================================================================
    //  HELPERS
    // =====================================================================

    /** Who gets student-related news: the student and the parent. */
    private List<User> audience(Student s) {
        return Stream.of(s.getUser(), s.getParent()).filter(Objects::nonNull).toList();
    }

    private List<User> staff() {
        return userRepo.findByRole(Role.ROLE_ADMIN);
    }

    /** The only place that decides which name the AI sees (change to firstName for more privacy). */
    private String name(Student s) {
        return s.getFirstName() + " " + s.getLastName();
    }

    private String teacherName(Course c) {
        return c.getTeacher() == null ? null
                : c.getTeacher().getFirstName() + " " + c.getTeacher().getLastName();
    }

    /** Lessons the student's classroom has on that weekday, taken from the timetable. */
    private String lessonsOn(Student s, LocalDate date) {
        if (s.getClassroom() == null) return null;
        String lessons = scheduleRepo.findForGrid(s.getClassroom().getId(), null).stream()
                .filter(l -> l.getDayOfWeek() == date.getDayOfWeek())
                .map(l -> l.getCourse().getTitle())
                .distinct()
                .collect(Collectors.joining(", "));
        return lessons.isEmpty() ? null : lessons;
    }

    private Map<String, String> facts(String... keyValues) {
        Map<String, String> m = new LinkedHashMap<>();
        for (int i = 0; i + 1 < keyValues.length; i += 2) m.put(keyValues[i], keyValues[i + 1]);
        return m;
    }

    private String clean(String t) {
        String s = t.replaceAll("\\s+", " ").trim();
        if (s.length() > 1 && s.startsWith("\"") && s.endsWith("\"")) s = s.substring(1, s.length() - 1);
        return s.length() > 400 ? s.substring(0, 397) + "..." : s;
    }

    /** A notification problem must never break the real action. */
    private void safely(Runnable r) {
        try { r.run(); }
        catch (Exception e) { log.error("❌ [NOTIFY] failed: {}", e.getMessage(), e); }
    }
}