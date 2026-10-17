package com.example.School.Management.System.assistant;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class AssistantTestController {

    @GetMapping("/ping")
    public ResponseEntity<Map<String, Object>> ping() {
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "JSON response is working correctly!",
                "timestamp", System.currentTimeMillis()
        ));
    }
}
