package com.example.School.Management.System.assistant;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/assistant")
@RequiredArgsConstructor
public class AssistantApiController {

    private final AssistantService service;

    @PostMapping("/ask")
    public AssistantAnswer ask(@RequestBody AskRequest req) {
        String q = req.question() == null ? "" : req.question().trim();
        if (q.isEmpty() || q.length() > 300) {
            return AssistantAnswer.message("Please write a question of up to 300 characters.");
        }
        return service.ask(q);
    }
}