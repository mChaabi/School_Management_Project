package com.example.School.Management.System.service;

import com.example.School.Management.System.entity.Exam;
import com.example.School.Management.System.entity.ExamFile;
import com.example.School.Management.System.repository.EnrollmentRepository;
import com.example.School.Management.System.repository.ExamFileInfo;
import com.example.School.Management.System.repository.ExamFileRepository;
import com.example.School.Management.System.repository.ExamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ExamFileService {

    private static final long MAX_BYTES = 10L * 1024 * 1024;

    private static final Map<String, String> TYPES = Map.of(
            "pdf",  "application/pdf",
            "doc",  "application/msword",
            "docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "odt",  "application/vnd.oasis.opendocument.text",
            "txt",  "text/plain; charset=UTF-8",
            "png",  "image/png",
            "jpg",  "image/jpeg",
            "jpeg", "image/jpeg");

    private final ExamFileRepository files;
    private final ExamRepository exams;
    private final EnrollmentRepository enrollments;
    private final ExamService examService;

    @Transactional(readOnly = true)
    public Optional<ExamFileInfo> info(Long examId) {
        return files.findByExamId(examId, ExamFileInfo.class);
    }

    /** Saves the file; an existing file of the same exam is replaced. */
    @Transactional
    public void upload(Long examId, MultipartFile file, boolean publish, String username) throws IOException {
        if (file == null || file.isEmpty())
            throw new IllegalArgumentException("Please choose a file.");
        if (file.getSize() > MAX_BYTES)
            throw new IllegalArgumentException("The file is too large. Maximum size is 10 MB.");

        String name = cleanName(file.getOriginalFilename());
        String ext = name.contains(".") ? name.substring(name.lastIndexOf('.') + 1).toLowerCase() : "";
        String type = TYPES.get(ext);
        if (type == null)
            throw new IllegalArgumentException("File type not allowed. Use PDF, Word, ODT, TXT, PNG or JPG.");

        byte[] data = file.getBytes();
        checkSignature(ext, data);

        Exam exam = exams.findById(examId).orElseThrow();
        files.deleteByExamId(examId);                      // replace the old file, if any

        ExamFile f = new ExamFile();
        f.setExam(exam);
        f.setFileName(name);
        f.setContentType(type);
        f.setSize(data.length);
        f.setData(data);
        f.setUploadedAt(LocalDateTime.now());
        f.setUploadedBy(username);
        f.setPublished(publish);
        files.save(f);
    }

    @Transactional
    public void setPublished(Long examId, boolean published) {
        ExamFile f = files.findByExamId(examId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        f.setPublished(published);
    }

    @Transactional
    public void delete(Long examId) {
        files.deleteByExamId(examId);
    }

    /** Returns the file if this user is allowed to see it. */
    @Transactional(readOnly = true)
    public ExamFile open(Long examId, Authentication auth) {
        ExamFile f = files.findByExamId(examId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        Long courseId = examService.courseIdOf(examId);

        boolean admin = hasRole(auth, "ROLE_ADMIN");
        boolean teacher = hasRole(auth, "ROLE_TEACHER");

        if (admin) return f;
        if (teacher) {
            examService.checkCourseAccess(courseId, auth);          // only the teacher of the course
            return f;
        }
        // student or parent: published and enrolled
        boolean enrolled = enrollments.existsByCourseIdAndStudentUserUsername(courseId, auth.getName())
                || enrollments.existsByCourseIdAndStudentParentUsername(courseId, auth.getName());
        if (!f.isPublished() || !enrolled)
            throw new AccessDeniedException("This exam paper is not available to you");
        return f;
    }

    // ---------- helpers ----------
    private boolean hasRole(Authentication auth, String role) {
        return auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals(role));
    }

    private String cleanName(String original) {
        String n = original == null ? "exam" : original.replace('\\', '/');
        n = n.substring(n.lastIndexOf('/') + 1).replaceAll("[\\p{Cntrl}\"]", "").trim();
        if (n.isEmpty()) n = "exam";
        return n.length() > 150 ? n.substring(n.length() - 150) : n;
    }

    /** The first bytes must match the extension, so a renamed program is refused. */
    private void checkSignature(String ext, byte[] d) {
        boolean ok = switch (ext) {
            case "pdf"          -> d.length > 4 && d[0] == '%' && d[1] == 'P' && d[2] == 'D' && d[3] == 'F';
            case "docx", "odt"  -> d.length > 3 && d[0] == 'P' && d[1] == 'K';
            case "doc"          -> d.length > 3 && (d[0] & 0xFF) == 0xD0 && (d[1] & 0xFF) == 0xCF;
            case "png"          -> d.length > 3 && (d[0] & 0xFF) == 0x89 && d[1] == 'P';
            case "jpg", "jpeg"  -> d.length > 2 && (d[0] & 0xFF) == 0xFF && (d[1] & 0xFF) == 0xD8;
            default             -> true;       // txt
        };
        if (!ok) throw new IllegalArgumentException("The file content does not match its type.");
    }
}
