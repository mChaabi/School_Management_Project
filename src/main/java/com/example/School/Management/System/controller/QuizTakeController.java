package com.example.School.Management.System.controller;

import com.example.School.Management.System.entity.QuizQuestion;
import com.example.School.Management.System.service.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class QuizTakeController {

    private final QuizService service;

    @GetMapping("/my-quizzes")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER','STUDENT')")
    public String mine(Authentication auth, Model model) {
        model.addAttribute("quizzes", service.myQuizzes(auth));
        return "quiz/my";
    }

    @GetMapping("/quizzes/{id}/take")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER','STUDENT')")
    public String take(@PathVariable Long id, Authentication auth, Model model, RedirectAttributes ra) {
        try {
            var view = service.start(id, auth);
            if (view == null) return "redirect:/quizzes/" + id + "/result";    // time already over
            model.addAttribute("quiz", view);
            return "quiz/take";
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/my-quizzes";
        }
    }

    @PostMapping("/quizzes/{id}/submit")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER','STUDENT')")
    public String submit(@PathVariable Long id, @RequestParam Map<String, String> form,
                         Authentication auth, RedirectAttributes ra) {
        Map<Long, Long> chosen = new HashMap<>();
        form.forEach((k, v) -> {
            if (k.startsWith("q_")) {
                try { chosen.put(Long.valueOf(k.substring(2)), Long.valueOf(v)); }
                catch (NumberFormatException ignored) { }
            }
        });
        try {
            service.submit(id, chosen, auth);
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/my-quizzes";
        }
        return "redirect:/quizzes/" + id + "/result";
    }

    @GetMapping("/quizzes/{id}/result")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER','STUDENT')")
    public String result(@PathVariable Long id, Authentication auth, Model model, RedirectAttributes ra) {
        try {
            model.addAttribute("r", service.result(id, auth));
            return "quiz/result";
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/my-quizzes";
        }
    }

    @GetMapping("/quizzes/questions/{qid}/image")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER','STUDENT')")
    public ResponseEntity<byte[]> image(@PathVariable Long qid, Authentication auth) {
        QuizQuestion q = service.imageFor(qid, auth);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(q.getImageType()))
                .cacheControl(CacheControl.noStore())
                .body(q.getImage());
    }
}