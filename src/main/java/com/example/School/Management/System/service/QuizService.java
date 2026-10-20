package com.example.School.Management.System.service;

import com.example.School.Management.System.entity.*;
import com.example.School.Management.System.repository.*;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuizService {

    private static final long MAX_IMPORT = 5L * 1024 * 1024; // 5 MB
    private static final long MAX_IMAGE = 2L * 1024 * 1024;  // 2 MB

    private final QuizRepository quizzes;
    private final QuizAttemptRepository attempts;
    private final CourseRepository courses;
    private final EnrollmentRepository enrollments;
    private final StudentRepository students;
    private final ExamService examService;

    // ----- data for the pages (records) -----
    public record QuizRow(Long id, String title, int minutes, boolean published, int questions, long attempts) {}
    public record OptionView(Long id, String text, boolean correct) {}
    public record QuestionView(Long id, String text, boolean hasImage, List<OptionView> options) {}
    public record QuizView(Long id, Long courseId, String courseTitle, String title, int minutes,
                           boolean published, long attempts, List<QuestionView> questions) {}
    public record TakeOption(Long id, String text) {}
    public record TakeQuestion(Long id, String text, boolean hasImage, List<TakeOption> options) {}
    public record TakeView(Long id, String title, String courseTitle, long secondsLeft, List<TakeQuestion> questions) {}
    public record MyQuizRow(Long id, String title, String courseTitle, int minutes, int questions,
                            String status, Integer score, Integer total) {}
    public record AttemptRow(String student, boolean submitted, Integer score, Integer total, int percent, LocalDateTime at) {}
    public record ReviewRow(String question, String chosen, boolean correct) {}
    public record ResultView(String title, String courseTitle, int score, int total, int percent,
                             boolean passed, List<ReviewRow> review) {}
    public record ImportResult(int imported, List<String> warnings) {}

    // =============== staff ===============
    @Transactional(readOnly = true)
    public List<QuizRow> list(Long courseId) {
        return quizzes.findByCourseIdOrderByIdDesc(courseId).stream()
                .map(q -> new QuizRow(q.getId(), q.getTitle(), q.getDurationMinutes(), q.isPublished(),
                        q.getQuestions().size(), attempts.countByQuizId(q.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public Long courseIdOfQuiz(Long quizId) { return quiz(quizId).getCourse().getId(); }

    @Transactional(readOnly = true)
    public String courseTitle(Long courseId) { return courses.findById(courseId).orElseThrow().getTitle(); }

    @Transactional
    public Quiz create(Long courseId, String title, int minutes) {
        if (title == null || title.isBlank()) throw new IllegalArgumentException("The quiz title is mandatory.");
        if (minutes < 1 || minutes > 240) throw new IllegalArgumentException("The duration must be between 1 and 240 minutes.");
        Quiz q = new Quiz();
        q.setTitle(title.trim());
        q.setDurationMinutes(minutes);
        q.setCourse(courses.findById(courseId).orElseThrow());
        return quizzes.save(q);
    }

    @Transactional(readOnly = true)
    public QuizView view(Long quizId) {
        Quiz q = quiz(quizId);
        return new QuizView(q.getId(), q.getCourse().getId(), q.getCourse().getTitle(), q.getTitle(),
                q.getDurationMinutes(), q.isPublished(), attempts.countByQuizId(quizId),
                q.getQuestions().stream().map(qq -> new QuestionView(qq.getId(), qq.getText(), qq.getImage() != null,
                                qq.getOptions().stream().map(o -> new OptionView(o.getId(), o.getText(), o.isCorrect())).toList()))
                        .toList());
    }

    @Transactional
    public void addQuestion(Long quizId, String text, List<String> rawOptions, int correct, MultipartFile image)
            throws IOException {
        Quiz quiz = editable(quizId);
        if (text == null || text.isBlank()) throw new IllegalArgumentException("Write the question.");

        QuizQuestion qq = new QuizQuestion();
        qq.setQuiz(quiz);
        qq.setText(text.trim());
        qq.setPosition(quiz.getQuestions().size() + 1);
        for (int i = 0; i < rawOptions.size(); i++) {
            String t = rawOptions.get(i) == null ? "" : rawOptions.get(i).trim();
            if (t.isEmpty()) continue;
            QuizOption o = new QuizOption();
            o.setQuestion(qq);
            o.setText(t.length() > 500 ? t.substring(0, 500) : t);
            o.setCorrect(i == correct);
            qq.getOptions().add(o);
        }
        if (qq.getOptions().size() < 2) throw new IllegalArgumentException("Fill at least 2 options.");
        if (qq.getOptions().stream().noneMatch(QuizOption::isCorrect))
            throw new IllegalArgumentException("Choose the correct answer among the filled options.");

        if (image != null && !image.isEmpty()) {
            String name = image.getOriginalFilename() == null ? "" : image.getOriginalFilename().toLowerCase();
            byte[] d = image.getBytes();
            boolean png = name.endsWith(".png") && d.length > 3 && (d[0] & 0xFF) == 0x89 && d[1] == 'P';
            boolean jpg = (name.endsWith(".jpg") || name.endsWith(".jpeg")) && d.length > 2
                    && (d[0] & 0xFF) == 0xFF && (d[1] & 0xFF) == 0xD8;
            if (!png && !jpg) throw new IllegalArgumentException("The image must be a real JPG or PNG file.");
            if (d.length > MAX_IMAGE) throw new IllegalArgumentException("The image is too large (maximum 2 MB).");
            qq.setImage(d);
            qq.setImageType(png ? "image/png" : "image/jpeg");
        }
        quiz.getQuestions().add(qq);
    }

    @Transactional
    public ImportResult importFile(Long quizId, MultipartFile file) {
        Quiz quiz = editable(quizId);
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("Please choose a file.");
        if (file.getSize() > MAX_IMPORT) throw new IllegalArgumentException("The file is too large (maximum 5 MB).");

        QuizParser.Result parsed = QuizParser.parse(extractText(file));
        if (parsed.questions().isEmpty())
            throw new IllegalArgumentException("No question was found. Check the format shown on the page."
                    + (parsed.warnings().isEmpty() ? "" : " " + parsed.warnings().get(0)));
        if (parsed.questions().size() > 100) throw new IllegalArgumentException("Maximum 100 questions per import.");

        for (QuizParser.Parsed p : parsed.questions()) {
            QuizQuestion qq = new QuizQuestion();
            qq.setQuiz(quiz);
            qq.setText(p.text());
            qq.setPosition(quiz.getQuestions().size() + 1);
            for (int i = 0; i < p.options().size(); i++) {
                QuizOption o = new QuizOption();
                o.setQuestion(qq);
                String t = p.options().get(i);
                o.setText(t.length() > 500 ? t.substring(0, 500) : t);
                o.setCorrect(i == p.correct());
                qq.getOptions().add(o);
            }
            quiz.getQuestions().add(qq);
        }
        return new ImportResult(parsed.questions().size(), parsed.warnings());
    }

    @Transactional(readOnly = true)
    public Long quizIdOfQuestion(Long questionId) {
        return findQuestion(questionId).getQuiz().getId();
    }

    @Transactional
    public void deleteQuestion(Long questionId) {
        QuizQuestion qq = findQuestion(questionId);
        Quiz quiz = editable(qq.getQuiz().getId());
        quiz.getQuestions().removeIf(x -> x.getId().equals(questionId));
    }

    @Transactional
    public void setPublished(Long quizId, boolean publish) {
        Quiz q = quiz(quizId);
        if (publish && q.getQuestions().isEmpty())
            throw new IllegalArgumentException("Add at least one question before publishing.");
        q.setPublished(publish);
    }

    @Transactional
    public void deleteQuiz(Long quizId) {
        attempts.deleteByQuizId(quizId);
        quizzes.delete(quiz(quizId));
    }

    @Transactional(readOnly = true)
    public List<AttemptRow> attemptRows(Long quizId) {
        return attempts.findByQuizIdOrderByScoreDescSubmittedAtAsc(quizId).stream()
                .map(a -> new AttemptRow(a.getStudent().getFirstName() + " " + a.getStudent().getLastName(),
                        a.getSubmittedAt() != null, a.getScore(), a.getTotal(), percent(a.getScore(), a.getTotal()),
                        a.getSubmittedAt()))
                .toList();
    }

    @Transactional(readOnly = true)
    public String quizTitle(Long quizId) { return quiz(quizId).getTitle(); }

    @Transactional(readOnly = true)
    public List<MyQuizRow> myQuizzes(Authentication auth) {
        boolean isStaff = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_TEACHER"));

        Student s = null;
        if (isStaff) {
            // Para que el Admin pueda probar, usa el primer estudiante de prueba en el sistema
            s = students.findAll().stream().findFirst().orElse(null);
        } else {
            s = me(auth);
        }

        if (s == null) {
            return quizzes.findAll().stream().map(q -> new MyQuizRow(
                    q.getId(), q.getTitle(), q.getCourse().getTitle(), q.getDurationMinutes(),
                    q.getQuestions().size(), "TODO", null, null
            )).toList();
        }

        Map<Long, QuizAttempt> mine = attempts.findByStudentId(s.getId()).stream()
                .collect(Collectors.toMap(a -> a.getQuiz().getId(), a -> a));

        List<Quiz> availableQuizzes = isStaff ? quizzes.findAll() : quizzes.availableFor(s.getId());

        return availableQuizzes.stream().map(q -> {
            QuizAttempt a = mine.get(q.getId());
            String status = a == null ? "TODO" : (a.getSubmittedAt() == null ? "IN_PROGRESS" : "DONE");
            return new MyQuizRow(q.getId(), q.getTitle(), q.getCourse().getTitle(), q.getDurationMinutes(),
                    q.getQuestions().size(), status, a == null ? null : a.getScore(), a == null ? null : a.getTotal());
        }).toList();
    }

    /** Starts (or resumes) the quiz. Handles both students and testing staff. */
    @Transactional
    public TakeView start(Long quizId, Authentication auth) {
        Quiz q = quiz(quizId);
        boolean isStaff = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_TEACHER"));

        Student s = null;
        if (!isStaff) {
            s = me(auth);
            openFor(quizId, s);
        } else {
            // For staff preview/testing, create a dummy or skip enrollment check
            s = students.findAll().stream().findFirst().orElse(null);
            if (s == null) throw new IllegalStateException("No student profile available in the system for testing.");
        }

        QuizAttempt a = attempts.findByQuizIdAndStudentId(quizId, s.getId()).orElse(null);
        if (a != null && a.getSubmittedAt() != null) throw new IllegalStateException("You already completed this quiz.");
        if (a == null) {
            a = new QuizAttempt();
            a.setQuiz(q); a.setStudent(s); a.setStartedAt(LocalDateTime.now());
            attempts.save(a);
        }

        long left = (q.getDurationMinutes() * 60L) - Duration.between(a.getStartedAt(), LocalDateTime.now()).getSeconds();
        if (left <= 0) { grade(a, q, Map.of()); return null; }

        return new TakeView(q.getId(), q.getTitle(), q.getCourse().getTitle(), left,
                q.getQuestions().stream().map(qq -> new TakeQuestion(qq.getId(), qq.getText(), qq.getImage() != null,
                        qq.getOptions().stream().map(o -> new TakeOption(o.getId(), o.getText())).toList())).toList());
    }

    @Transactional
    public void submit(Long quizId, Map<Long, Long> chosen, Authentication auth) {
        Student s = me(auth);
        Quiz q = openFor(quizId, s);
        QuizAttempt a = attempts.findByQuizIdAndStudentId(quizId, s.getId())
                .orElseThrow(() -> new IllegalStateException("Start the quiz first."));
        if (a.getSubmittedAt() != null) return;
        long used = Duration.between(a.getStartedAt(), LocalDateTime.now()).getSeconds();
        boolean inTime = used <= q.getDurationMinutes() * 60L + 60;      // 1 minute of grace
        grade(a, q, inTime ? chosen : Map.of());
    }

    @Transactional(readOnly = true)
    public ResultView result(Long quizId, Authentication auth) {
        Student s = me(auth);
        QuizAttempt a = attempts.findByQuizIdAndStudentId(quizId, s.getId())
                .orElseThrow(() -> new IllegalStateException("You have not taken this quiz."));
        if (a.getSubmittedAt() == null) throw new IllegalStateException("This quiz is not finished yet.");
        int pct = percent(a.getScore(), a.getTotal());
        return new ResultView(a.getQuiz().getTitle(), a.getQuiz().getCourse().getTitle(), a.getScore(), a.getTotal(), pct,
                pct >= 50,
                a.getAnswers().stream()
                        .sorted(Comparator.comparing(x -> x.getQuestion().getPosition()))
                        .map(x -> new ReviewRow(x.getQuestion().getText(),
                                x.getSelected() == null ? "No answer" : x.getSelected().getText(), x.isCorrect()))
                        .toList());
    }

    /** Image of a question: staff of the course, or an enrolled student of a published quiz. */
    @Transactional(readOnly = true)
    public QuizQuestion imageFor(Long questionId, Authentication auth) {
        QuizQuestion qq = findQuestion(questionId);
        if (qq.getImage() == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        boolean staff = auth.getAuthorities().stream()
                .anyMatch(g -> g.getAuthority().equals("ROLE_ADMIN") || g.getAuthority().equals("ROLE_TEACHER"));
        if (staff) examService.checkCourseAccess(qq.getQuiz().getCourse().getId(), auth);
        else openFor(qq.getQuiz().getId(), me(auth));
        return qq;
    }

    // =============== helpers ===============
    private void grade(QuizAttempt a, Quiz q, Map<Long, Long> chosen) {
        int score = 0;
        for (QuizQuestion qq : q.getQuestions()) {
            Long optId = chosen.get(qq.getId());
            QuizOption sel = optId == null ? null
                    : qq.getOptions().stream().filter(o -> o.getId().equals(optId)).findFirst().orElse(null);
            boolean ok = sel != null && sel.isCorrect();
            if (ok) score++;
            QuizAnswer an = new QuizAnswer();
            an.setAttempt(a); an.setQuestion(qq); an.setSelected(sel); an.setCorrect(ok);
            a.getAnswers().add(an);
        }
        a.setScore(score);
        a.setTotal(q.getQuestions().size());
        a.setSubmittedAt(LocalDateTime.now());
    }

    private Quiz openFor(Long quizId, Student s) {
        Quiz q = quiz(quizId);
        if (!q.isPublished() || !enrollments.existsByCourseIdAndStudentId(q.getCourse().getId(), s.getId()))
            throw new AccessDeniedException("This quiz is not available to you");
        return q;
    }

    private Quiz editable(Long quizId) {
        if (attempts.countByQuizId(quizId) > 0)
            throw new IllegalArgumentException("Students already started this quiz. Its questions can no longer be changed.");
        return quiz(quizId);
    }

    private Student me(Authentication auth) {
        return students.findByUserUsername(auth.getName())
                .orElseThrow(() -> new AccessDeniedException("Your account is not linked to a student"));
    }

    private Quiz quiz(Long id) {
        return quizzes.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    private QuizQuestion findQuestion(Long id) {
        return quizzes.findAll().stream().flatMap(q -> q.getQuestions().stream())
                .filter(x -> x.getId().equals(id)).findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    private int percent(Integer score, Integer total) {
        return (score == null || total == null || total == 0) ? 0 : (int) Math.round(score * 100.0 / total);
    }

    private String extractText(MultipartFile file) {
        String name = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase();
        try {
            byte[] d = file.getBytes();
            if (name.endsWith(".txt")) return new String(d, StandardCharsets.UTF_8);
            if (name.endsWith(".docx")) {
                if (d.length < 4 || d[0] != 'P' || d[1] != 'K') throw new IllegalArgumentException("This is not a real Word file.");
                try (XWPFDocument doc = new XWPFDocument(new ByteArrayInputStream(d))) {
                    return doc.getParagraphs().stream().map(XWPFParagraph::getText).collect(Collectors.joining("\n"));
                }
            }
            if (name.endsWith(".pdf")) {
                if (d.length < 5 || d[0] != '%' || d[1] != 'P') throw new IllegalArgumentException("This is not a real PDF file.");
                try (PDDocument doc = Loader.loadPDF(d)) { return new PDFTextStripper().getText(doc); }
            }
            throw new IllegalArgumentException("To import questions use a TXT, DOCX or PDF file.");
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (IOException | RuntimeException e) {
            throw new IllegalArgumentException("The file could not be read.");
        }
    }
}