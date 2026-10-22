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

import java.math.BigDecimal;
import java.math.RoundingMode;
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

    @Value("${school.currency:MAD}")
    private String currency;

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

    /** Report card or student profile issued. The person who did it is not notified. */
    @Async
    public void onDocumentIssued(Long studentId, String document, String actor) {
        safely(() -> {
            Student s = studentRepo.findForNotification(studentId).orElseThrow();
            LocalDate today = LocalDate.now();
            List<User> to = audience(s).stream()
                    .filter(u -> !u.getUsername().equals(actor))
                    .toList();
            notify(NotificationType.DOCUMENT_ISSUED,
                    facts("student", name(s), "document", document, "date", today.format(DATE)),
                    to, null,                                        // sin enlace: cada rol tiene su página
                    "DOC:" + document.replace(' ', '_') + ":" + studentId + ":" + today);  // 1 por día
        });
    }

    /** One summary for the whole import, never one per student. */
    @Async
    public void onStudentsImported(int total, int created, int failed, String actor) {
        safely(() -> notify(NotificationType.IMPORT_COMPLETED,
                facts("total", String.valueOf(total), "created", String.valueOf(created),
                        "failed", String.valueOf(failed), "by", actor),
                staff(), "/students", null));
    }

    /** Holiday or event added to the calendar. Pass plain values: the DB transaction may not be committed yet. */
    @Async
    public void onSchoolEvent(Long eventId, String type, String title,
                              LocalDate start, LocalDate end, String description) {
        safely(() -> {
            String dates = start.equals(end)
                    ? start.format(DATE)
                    : start.format(DATE) + " - " + end.format(DATE);
            notify(NotificationType.SCHOOL_EVENT,
                    facts("kind", "HOLIDAY".equals(type) ? "Holiday" : "Event",
                            "title", title, "dates", dates, "description", description),
                    everyone(), "/calendar", "SCHOOL_EVENT:" + eventId);
        });
    }

    @Async
    public void onFeeCreated(Long feeId, Long studentId, String title, BigDecimal amount, LocalDate dueDate) {
        safely(() -> {
            Student s = studentRepo.findForNotification(studentId).orElseThrow();
            notify(NotificationType.FEE_CREATED,
                    facts("student", name(s), "title", title, "amount", money(amount),
                            "dueDate", dueDate.format(DATE)),
                    parentOf(s), "/parent/fees", "FEE_NEW:" + feeId);
        });
    }

    @Async
    public void onFeePaid(Long feeId, Long studentId, String title, BigDecimal amount, String receipt) {
        safely(() -> {
            Student s = studentRepo.findForNotification(studentId).orElseThrow();
            notify(NotificationType.FEE_PAID,
                    facts("student", name(s), "title", title, "amount", money(amount), "receipt", receipt),
                    parentOf(s), "/parent/fees", "FEE_PAID:" + feeId);
        });
    }

    @Async
    public void onFeeOverdue(Long feeId, Long studentId, String title, BigDecimal amount, LocalDate dueDate) {
        safely(() -> {
            Student s = studentRepo.findForNotification(studentId).orElseThrow();
            notify(NotificationType.FEE_OVERDUE,
                    facts("student", name(s), "title", title, "amount", money(amount),
                            "dueDate", dueDate.format(DATE)),
                    parentOf(s), "/parent/fees", "FEE_OVERDUE:" + feeId);   // un solo aviso por cuota
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
            List<User> to = new ArrayList<>(staff());          // <-- add this line
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

    /** Who gets student-related news: admins + the student + the parent (if they exist). */
    private List<User> audience(Student s) {
        List<User> list = new ArrayList<>(staff());
        if (s.getUser() != null)   list.add(s.getUser());
        if (s.getParent() != null) list.add(s.getParent());
        return list;
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

    /** Every active account: admins, teachers, students and parents. */
    private List<User> everyone() {
        return Stream.of(Role.values())
                .flatMap(r -> userRepo.findByRole(r).stream())
                .filter(User::isEnabled)
                .toList();
    }

    /** Fees are private: only the parent, never teachers. */
    private List<User> parentOf(Student s) {
        return s.getParent() == null ? List.of() : List.of(s.getParent());
    }

    private String money(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP).toPlainString() + " " + currency;
    }
}