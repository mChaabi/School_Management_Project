package com.example.School.Management.System.assistant;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/assistant")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
@Slf4j
public class AssistantController {

    private final AssistantService service;

    @GetMapping
    public String page() { return "assistant/chat"; }

    @PostMapping("/ask")
    @ResponseBody
    public AssistantAnswer ask(@RequestBody AskRequest req) {
        String q = req.question() == null ? "" : req.question().trim();
        if (q.isEmpty() || q.length() > 300) {
            return AssistantAnswer.message("Please write a question of up to 300 characters.");
        }
        try {
            return service.ask(q);
        } catch (Exception e) {
            log.error("Error processing AI Assistant request: ", e);
            return AssistantAnswer.message("An error occurred while communicating with the AI service: " + e.getMessage());
        }
    }
}