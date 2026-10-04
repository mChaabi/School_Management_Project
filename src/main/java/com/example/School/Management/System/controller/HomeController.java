package com.example.School.Management.System.controller;

import org.springframework.ui.Model;
import com.example.School.Management.System.repository.CourseRepository;
import com.example.School.Management.System.repository.EnrollmentRepository;
import com.example.School.Management.System.repository.StudentRepository;
import com.example.School.Management.System.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("studentCount", studentRepository.count());
        model.addAttribute("teacherCount", teacherRepository.count());
        model.addAttribute("courseCount", courseRepository.count());
        model.addAttribute("enrollmentCount", enrollmentRepository.count());
        return "index";
    }
}