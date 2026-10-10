package com.example.School.Management.System.controller;

import com.example.School.Management.System.service.DashboardService;
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
    private final DashboardService dashboardService;

    @GetMapping({"/", "/dashboard"})
    public String dashboard(Model model) {
        model.addAttribute("d", dashboardService.build());
        return "index";
    }
}