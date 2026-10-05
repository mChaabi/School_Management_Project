package com.example.School.Management.System.api;

import com.example.School.Management.System.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsRestController {

    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;

    @GetMapping
    public Map<String, Long> stats() {
        return Map.of(
                "students", studentRepository.count(),
                "teachers", teacherRepository.count(),
                "courses", courseRepository.count(),
                "enrollments", enrollmentRepository.count());
    }
}