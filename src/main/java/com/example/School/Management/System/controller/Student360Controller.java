package com.example.School.Management.System.controller;

import com.example.School.Management.System.dto.StudentDto;
import com.example.School.Management.System.dto.StudentSuggestDto;
import com.example.School.Management.System.service.NotificationService;
import com.example.School.Management.System.service.Student360Service;
import com.example.School.Management.System.service.StudentServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/admin/student-360")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class Student360Controller {
    private final Student360Service service;
    private final StudentServiceImpl studentService;
    private final NotificationService notifications;      // inyéctalo

    @GetMapping
    public String search(@RequestParam(required = false) String keyword,
                         @RequestParam(defaultValue = "0") int page, Model model) {
        if (keyword != null && !keyword.isBlank()) {
            model.addAttribute("results", service.search(keyword, PageRequest.of(page, 10)));
        }
        model.addAttribute("keyword", keyword);
        return "admin/student-360-search";
    }

    @GetMapping("/{id}")
    public String profile(@PathVariable Long id, Model model) {
        model.addAttribute("p", service.getProfile(id));
        return "admin/student-360-profile";
    }

    @PostMapping("/{id}/printed")
    @ResponseBody
    public ResponseEntity<Void> printed(@PathVariable Long id, Principal principal) {
        notifications.onDocumentIssued(id, "student profile", principal.getName());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/suggest")
    @ResponseBody
    public List<StudentSuggestDto> suggest(@RequestParam("q") String q) {
        return service.suggest(q);
    }

    @GetMapping("/search-ajax") // Keep it short so it resolves to /admin/student-360/search-ajax
    @ResponseBody
    public List<StudentDto> searchStudentsAjax(@RequestParam("keyword") String keyword) {
        return studentService.searchStudents(keyword, PageRequest.of(0, 10)).getContent();
    }
}
