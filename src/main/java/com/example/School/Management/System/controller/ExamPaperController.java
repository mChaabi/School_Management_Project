package com.example.School.Management.System.controller;

import com.example.School.Management.System.entity.ExamFile;
import com.example.School.Management.System.service.ExamFileService;
import com.example.School.Management.System.service.ExamService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Controller
@RequiredArgsConstructor
public class ExamPaperController {

    private final ExamFileService service;
    private final ExamService exams;

    // ----- page: upload and manage (admin and teacher) -----
    @GetMapping("/exams/{examId}/paper")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public String page(@PathVariable Long examId, Authentication auth, Model model) {
        var header = exams.header(examId);
        exams.checkCourseAccess(header.courseId(), auth);
        model.addAttribute("exam", header);
        model.addAttribute("file", service.info(examId).orElse(null));
        return "exam/paper";
    }

    @PostMapping("/exams/{examId}/paper")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public String upload(@PathVariable Long examId,
                         @RequestParam("file") MultipartFile file,
                         @RequestParam(defaultValue = "false") boolean publish,
                         Authentication auth, RedirectAttributes ra) {
        exams.checkCourseAccess(exams.courseIdOf(examId), auth);
        try {
            service.upload(examId, file, publish, auth.getName());
            ra.addFlashAttribute("success", "Exam file saved.");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
        } catch (IOException e) {
            ra.addFlashAttribute("error", "The file could not be read.");
        }
        return "redirect:/exams/" + examId + "/paper";
    }

    @PostMapping("/exams/{examId}/paper/publish")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public String publish(@PathVariable Long examId, @RequestParam boolean publish,
                          Authentication auth, RedirectAttributes ra) {
        exams.checkCourseAccess(exams.courseIdOf(examId), auth);
        service.setPublished(examId, publish);
        ra.addFlashAttribute("success", publish ? "The exam is now visible to the students."
                : "The exam is hidden from the students.");
        return "redirect:/exams/" + examId + "/paper";
    }

    @PostMapping("/exams/{examId}/paper/delete")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public String delete(@PathVariable Long examId, Authentication auth, RedirectAttributes ra) {
        exams.checkCourseAccess(exams.courseIdOf(examId), auth);
        service.delete(examId);
        ra.addFlashAttribute("success", "Exam file deleted.");
        return "redirect:/exams/" + examId + "/paper";
    }

    // ----- read and export (staff, and enrolled students when published) -----
    @GetMapping("/exams/{examId}/paper/view")
    public ResponseEntity<byte[]> view(@PathVariable Long examId, Authentication auth) {
        return send(service.open(examId, auth), true);
    }

    @GetMapping("/exams/{examId}/paper/download")
    public ResponseEntity<byte[]> download(@PathVariable Long examId, Authentication auth) {
        return send(service.open(examId, auth), false);
    }

    private ResponseEntity<byte[]> send(ExamFile f, boolean inline) {
        ContentDisposition cd = (inline ? ContentDisposition.inline() : ContentDisposition.attachment())
                .filename(f.getFileName(), StandardCharsets.UTF_8).build();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, cd.toString())
                .contentType(MediaType.parseMediaType(f.getContentType()))
                .contentLength(f.getData().length)
                .body(f.getData());
    }

    // file bigger than the limit in application.properties
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String tooBig(HttpServletRequest request, RedirectAttributes ra) {
        ra.addFlashAttribute("error", "The file is too large. Maximum size is 10 MB.");
        return "redirect:" + request.getRequestURI();
    }
}