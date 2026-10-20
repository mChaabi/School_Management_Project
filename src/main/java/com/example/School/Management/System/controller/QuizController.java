package com.example.School.Management.System.controller;

import com.example.School.Management.System.service.ExamService;
import com.example.School.Management.System.service.QuizService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;

@Controller
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_TEACHER','ROLE_STUDENT')")
public class QuizController {

    private final QuizService service;
    private final ExamService exams;               // only for the "own courses" check

    @GetMapping("/courses/{courseId}/quizzes")
    public String list(@PathVariable Long courseId, Authentication auth, Model model) {
        exams.checkCourseAccess(courseId, auth);
        model.addAttribute("courseId", courseId);
        model.addAttribute("courseTitle", service.courseTitle(courseId));
        model.addAttribute("quizzes", service.list(courseId));
        return "quiz/list";
    }

    @PostMapping("/courses/{courseId}/quizzes")
    public String create(@PathVariable Long courseId, @RequestParam String title,
                         @RequestParam(defaultValue = "15") int minutes,
                         Authentication auth, RedirectAttributes ra) {
        exams.checkCourseAccess(courseId, auth);
        try {
            return "redirect:/quizzes/" + service.create(courseId, title, minutes).getId() + "/edit";
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/courses/" + courseId + "/quizzes";
        }
    }

    @GetMapping("/quizzes/{id}/edit")
    public String edit(@PathVariable Long id, Authentication auth, Model model) {
        exams.checkCourseAccess(service.courseIdOfQuiz(id), auth);
        model.addAttribute("quiz", service.view(id));
        return "quiz/edit";
    }

    @PostMapping("/quizzes/{id}/questions")
    public String addQuestion(@PathVariable Long id, @RequestParam String text,
                              @RequestParam(name = "option") List<String> options,
                              @RequestParam(defaultValue = "0") int correct,
                              @RequestParam(required = false) MultipartFile image,
                              Authentication auth, RedirectAttributes ra) {
        exams.checkCourseAccess(service.courseIdOfQuiz(id), auth);
        try {
            service.addQuestion(id, text, options, correct, image);
            ra.addFlashAttribute("success", "Question added.");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
        } catch (IOException e) {
            ra.addFlashAttribute("error", "The image could not be read.");
        }
        return "redirect:/quizzes/" + id + "/edit";
    }

    @PostMapping("/quizzes/{id}/import")
    public String importFile(@PathVariable Long id, @RequestParam MultipartFile file,
                             Authentication auth, RedirectAttributes ra) {
        exams.checkCourseAccess(service.courseIdOfQuiz(id), auth);
        try {
            var r = service.importFile(id, file);
            ra.addFlashAttribute("success", r.imported() + " question(s) imported."
                    + (r.warnings().isEmpty() ? "" : " " + String.join(" ", r.warnings())));
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/quizzes/" + id + "/edit";
    }

    @PostMapping("/quizzes/{id}/publish")
    public String publish(@PathVariable Long id, @RequestParam boolean publish,
                          Authentication auth, RedirectAttributes ra) {
        exams.checkCourseAccess(service.courseIdOfQuiz(id), auth);
        try {
            service.setPublished(id, publish);
            ra.addFlashAttribute("success", publish ? "The quiz is now open to the enrolled students."
                    : "The quiz is hidden from the students.");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/quizzes/" + id + "/edit";
    }

    @PostMapping("/quizzes/questions/{questionId}/delete")
    public String deleteQuestion(@PathVariable Long questionId, Authentication auth, RedirectAttributes ra) {
        Long quizId = service.quizIdOfQuestion(questionId);
        exams.checkCourseAccess(service.courseIdOfQuiz(quizId), auth);
        try {
            service.deleteQuestion(questionId);
            ra.addFlashAttribute("success", "Question deleted.");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/quizzes/" + quizId + "/edit";
    }

    @PostMapping("/quizzes/{id}/delete")
    public String delete(@PathVariable Long id, Authentication auth, RedirectAttributes ra) {
        Long courseId = service.courseIdOfQuiz(id);
        exams.checkCourseAccess(courseId, auth);
        service.deleteQuiz(id);
        ra.addFlashAttribute("success", "Quiz deleted.");
        return "redirect:/courses/" + courseId + "/quizzes";
    }

    @GetMapping("/quizzes/{id}/result")
    public String viewResult(@PathVariable Long id, Authentication auth, Model model) {
        QuizService.ResultView result = service.result(id, auth); // <-- Cambiado de Service a service
        model.addAttribute("r", result);
        return "quiz/result";
    }

    @GetMapping("/quizzes/{id}/attempts")
    public String attempts(@PathVariable Long id, Authentication auth, Model model) {
        exams.checkCourseAccess(service.courseIdOfQuiz(id), auth);
        model.addAttribute("quizId", id);
        model.addAttribute("quizTitle", service.quizTitle(id));
        model.addAttribute("rows", service.attemptRows(id));
        return "quiz/attempts";
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String tooBig(HttpServletRequest request, RedirectAttributes ra) {
        ra.addFlashAttribute("error", "The file is too large.");
        return "redirect:" + request.getRequestURI().replace("/import", "/edit").replace("/questions", "/edit");
    }
}
