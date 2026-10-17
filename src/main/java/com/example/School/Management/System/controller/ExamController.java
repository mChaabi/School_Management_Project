package com.example.School.Management.System.controller;

import com.example.School.Management.System.entity.Exam;
import com.example.School.Management.System.entity.ExamForm;
import com.example.School.Management.System.entity.ExamSheetDto;
import com.example.School.Management.System.service.ExamService;
import com.example.School.Management.System.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
public class ExamController {

    private final ExamService service;
    private final NotificationService notificationService;

    // ----- list of exams of a course -----
    @GetMapping("/courses/{courseId}/exams")
    public String list(@PathVariable Long courseId, Authentication auth, Model model) {
        service.checkCourseAccess(courseId, auth);
        model.addAttribute("courseId", courseId);
        model.addAttribute("courseTitle", service.courseTitle(courseId));
        model.addAttribute("exams", service.list(courseId));
        return "exam/list";
    }

    // ----- create an exam -----
    @GetMapping("/courses/{courseId}/exams/new")
    public String newForm(@PathVariable Long courseId, Authentication auth, Model model) {
        service.checkCourseAccess(courseId, auth);
        model.addAttribute("examForm", new ExamForm());
        model.addAttribute("courseId", courseId);
        model.addAttribute("courseTitle", service.courseTitle(courseId));
        return "exam/form";
    }

    @PostMapping("/courses/{courseId}/exams")
    public String create(@PathVariable Long courseId,
                         @Valid @ModelAttribute("examForm") ExamForm form,
                         BindingResult result, Authentication auth, Model model) {
        service.checkCourseAccess(courseId, auth);
        if (!result.hasErrors()) {
            try {
                Exam saved = service.create(courseId, form);
                return "redirect:/exams/" + saved.getId() + "/grades";      // go straight to the scores
            } catch (IllegalArgumentException e) {
                result.rejectValue("title", "exam.duplicate", e.getMessage());
            }
        }
        model.addAttribute("courseId", courseId);
        model.addAttribute("courseTitle", service.courseTitle(courseId));
        return "exam/form";
    }

    // ----- score sheet -----
    @GetMapping("/exams/{examId}/grades")
    public String sheet(@PathVariable Long examId, Authentication auth, Model model) {
        service.checkCourseAccess(service.courseIdOf(examId), auth);
        model.addAttribute("sheet", service.loadSheet(examId));
        return "exam/sheet";
    }

    @PostMapping("/exams/{examId}/grades")
    public String save(@PathVariable Long examId,
                       @ModelAttribute("sheet") ExamSheetDto sheet,
                       Authentication auth, Model model, RedirectAttributes ra) {
        Long courseId = service.courseIdOf(examId);
        service.checkCourseAccess(courseId, auth);
        try {
            var graded = service.saveSheet(examId, sheet);
            graded.forEach(g -> notificationService.onGradeAdded(g.studentId(), courseId, g.score20()));
            ra.addFlashAttribute("success", "Scores saved. Final grades were updated.");
            return "redirect:/exams/" + examId + "/grades";
        } catch (IllegalArgumentException e) {
            model.addAttribute("sheet", service.withPostedValues(examId, sheet));
            model.addAttribute("error", e.getMessage());
            return "exam/sheet";
        }
    }

    @PostMapping("/exams/{examId}/delete")
    public String delete(@PathVariable Long examId, Authentication auth, RedirectAttributes ra) {
        Long courseId = service.courseIdOf(examId);
        service.checkCourseAccess(courseId, auth);
        service.delete(examId);
        ra.addFlashAttribute("success", "Exam deleted. Final grades were recalculated.");
        return "redirect:/courses/" + courseId + "/exams";
    }
}
