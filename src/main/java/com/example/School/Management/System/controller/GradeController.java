package com.example.School.Management.System.controller;

import com.example.School.Management.System.dto.GradeSheetDto;
import com.example.School.Management.System.service.GradeService;
import com.example.School.Management.System.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/courses/{courseId}/grades")
@RequiredArgsConstructor
public class GradeController {

    private final GradeService service;
    private final NotificationService notificationService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public String sheet(@PathVariable Long courseId, Model model) {
        GradeSheetDto sheet = service.loadSheet(courseId);
        model.addAttribute("sheet", sheet);
        return "course/grades";
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public String save(@PathVariable Long courseId,
                       @Valid @ModelAttribute("sheet") GradeSheetDto sheet,
                       BindingResult result, Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            return "course/grades";
        }
        service.save(courseId, sheet);

        // <-- ADD AI NOTIFICATION TRIGGER HERE -->
        // Loop through the grades in the sheet and trigger notification for each student
        if (sheet.getRows() != null) {
            for (var row : sheet.getRows()) {
                if (row.getGrade() != null) {
                    notificationService.onGradeAdded(row.getStudentId(), courseId, row.getGrade());
                }
            }
        }

        ra.addFlashAttribute("success", "Grades saved successfully.");
        return "redirect:/courses/" + courseId + "/grades";
    }
}