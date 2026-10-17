package com.example.School.Management.System.service;

import com.example.School.Management.System.entity.*;
import com.example.School.Management.System.repository.CourseRepository;
import com.example.School.Management.System.repository.EnrollmentRepository;
import com.example.School.Management.System.repository.ExamRepository;
import com.example.School.Management.System.repository.ExamResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExamService {

    private final ExamRepository exams;
    private final ExamResultRepository results;
    private final CourseRepository courses;
    private final EnrollmentRepository enrollments;

    public record ExamRow(Long id, String title, LocalDate date, Double maxScore,
                          Integer weight, long graded, long total) {}
    public record Graded(Long studentId, double score20) {}

    // ---------- access: admin = everything, teacher = own courses only ----------
    @Transactional(readOnly = true)
    public void checkCourseAccess(Long courseId, Authentication auth) {
        boolean admin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (admin) return;
        Teacher t = courses.findById(courseId).orElseThrow().getTeacher();
        if (t == null || t.getUser() == null || !t.getUser().getUsername().equals(auth.getName()))
            throw new AccessDeniedException("You can only manage the exams of your own courses");
    }

    @Transactional(readOnly = true)
    public Long courseIdOf(Long examId) {
        return exams.findById(examId).orElseThrow().getCourse().getId();
    }

    @Transactional(readOnly = true)
    public String courseTitle(Long courseId) {
        return courses.findById(courseId).orElseThrow().getTitle();
    }

    // ---------- exams ----------
    @Transactional(readOnly = true)
    public List<ExamRow> list(Long courseId) {
        long total = enrollments.countByCourseId(courseId);
        return exams.findByCourseIdOrderByExamDateDesc(courseId).stream()
                .map(e -> new ExamRow(e.getId(), e.getTitle(), e.getExamDate(), e.getMaxScore(),
                        e.getWeight(), results.countByExamId(e.getId()), total))
                .toList();
    }

    @Transactional
    public Exam create(Long courseId, ExamForm f) {
        String title = f.getTitle().trim();
        if (exams.existsByCourseIdAndTitleIgnoreCase(courseId, title))
            throw new IllegalArgumentException("An exam with this title already exists in this course");
        return exams.save(Exam.builder()
                .title(title).examDate(f.getExamDate())
                .maxScore(f.getMaxScore()).weight(f.getWeight())
                .course(courses.findById(courseId).orElseThrow()).build());
    }

    @Transactional
    public void delete(Long examId) {
        Exam e = exams.findById(examId).orElseThrow();
        Long courseId = e.getCourse().getId();
        exams.delete(e);
        exams.flush();
        recompute(courseId);
    }

    // ---------- grade sheet ----------
    @Transactional(readOnly = true)
    public ExamSheetDto loadSheet(Long examId) {
        Exam e = exams.findById(examId).orElseThrow();
        Map<Long, ExamResult> existing = results.findByExamId(examId).stream()
                .collect(Collectors.toMap(r -> r.getStudent().getId(), r -> r));

        ExamSheetDto dto = new ExamSheetDto();
        dto.setExamId(e.getId());
        dto.setCourseId(e.getCourse().getId());
        dto.setExamTitle(e.getTitle());
        dto.setCourseTitle(e.getCourse().getTitle());
        dto.setExamDate(e.getExamDate());
        dto.setMaxScore(e.getMaxScore());
        dto.setWeight(e.getWeight());

        // only students enrolled in the course can sit the exam
        enrollments.findByCourseId(e.getCourse().getId()).stream()
                .map(Enrollment::getStudent)
                .sorted(Comparator.comparing(Student::getLastName).thenComparing(Student::getFirstName))
                .forEach(s -> {
                    ExamResult r = existing.get(s.getId());
                    dto.getRows().add(new ExamSheetDto.Row(s.getId(),
                            s.getFirstName() + " " + s.getLastName(),
                            r == null ? null : r.getScore(),
                            r != null && r.isAbsent()));
                });
        return dto;
    }

    /** Re-display the sheet with what the teacher typed after a validation error. */
    @Transactional(readOnly = true)
    public ExamSheetDto withPostedValues(Long examId, ExamSheetDto posted) {
        ExamSheetDto fresh = loadSheet(examId);
        Map<Long, ExamSheetDto.Row> typed = new HashMap<>();
        posted.getRows().stream().filter(r -> r.getStudentId() != null)
                .forEach(r -> typed.putIfAbsent(r.getStudentId(), r));
        fresh.getRows().forEach(r -> {
            ExamSheetDto.Row p = typed.get(r.getStudentId());
            if (p != null) { r.setScore(p.getScore()); r.setAbsent(p.isAbsent()); }
        });
        return fresh;
    }

    @Transactional
    public List<Graded> saveSheet(Long examId, ExamSheetDto sheet) {
        Exam exam = exams.findById(examId).orElseThrow();
        Long courseId = exam.getCourse().getId();

        Map<Long, Student> enrolled = enrollments.findByCourseId(courseId).stream()
                .map(Enrollment::getStudent)
                .collect(Collectors.toMap(Student::getId, s -> s));
        Map<Long, ExamResult> existing = results.findByExamId(examId).stream()
                .collect(Collectors.toMap(r -> r.getStudent().getId(), r -> r));

        // 1. validate everything before saving anything
        for (ExamSheetDto.Row row : sheet.getRows()) {
            Student s = enrolled.get(row.getStudentId());
            if (s == null)
                throw new IllegalArgumentException("A student in the sheet is not enrolled in this course");
            if (!row.isAbsent() && row.getScore() != null
                    && (row.getScore() < 0 || row.getScore() > exam.getMaxScore()))
                throw new IllegalArgumentException(s.getFirstName() + " " + s.getLastName()
                        + ": the score must be between 0 and " + exam.getMaxScore());
        }

        // 2. save
        List<Graded> graded = new ArrayList<>();
        for (ExamSheetDto.Row row : sheet.getRows()) {
            ExamResult r = existing.get(row.getStudentId());

            if (!row.isAbsent() && row.getScore() == null) {      // blank = not graded yet
                if (r != null) results.delete(r);
                continue;
            }
            if (r == null)
                r = ExamResult.builder().exam(exam).student(enrolled.get(row.getStudentId())).build();

            r.setAbsent(row.isAbsent());
            r.setScore(row.isAbsent() ? null : row.getScore());
            results.save(r);

            if (!row.isAbsent())
                graded.add(new Graded(row.getStudentId(), round2(row.getScore() / exam.getMaxScore() * 20)));
        }
        results.flush();

        // 3. final grades
        recompute(courseId);
        return graded;
    }

    /** Final grade (out of 20) = weighted average of the exam results. Absent counts as 0. */
    private void recompute(Long courseId) {
        Map<Long, List<ExamResult>> byStudent = results.findByExamCourseId(courseId).stream()
                .collect(Collectors.groupingBy(r -> r.getStudent().getId()));

        for (Enrollment en : enrollments.findByCourseId(courseId)) {
            double sum = 0, weights = 0;
            for (ExamResult r : byStudent.getOrDefault(en.getStudent().getId(), List.of())) {
                double on20 = (r.isAbsent() || r.getScore() == null)
                        ? 0 : r.getScore() / r.getExam().getMaxScore() * 20;
                sum += on20 * r.getExam().getWeight();
                weights += r.getExam().getWeight();
            }
            en.setGrade(weights == 0 ? null : round2(sum / weights));   // saved by dirty checking
        }
    }

    private double round2(double v) { return Math.round(v * 100.0) / 100.0; }
}