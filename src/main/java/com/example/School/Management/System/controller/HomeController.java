package com.example.School.Management.System.controller;

import com.example.School.Management.System.dto.RiskStudentDto;
import com.example.School.Management.System.service.DashboardService;
import com.example.School.Management.System.service.RiskService;
import org.springframework.ui.Model;
import com.example.School.Management.System.repository.CourseRepository;
import com.example.School.Management.System.repository.EnrollmentRepository;
import com.example.School.Management.System.repository.StudentRepository;
import com.example.School.Management.System.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class HomeController {
    private final DashboardService dashboardService;
    private final RiskService riskService;

    @GetMapping({"/", "/dashboard"})
    public String dashboard(Model model) {
        List<RiskStudentDto> risk = riskService.findAtRisk();
        model.addAttribute("riskTotal", risk.size());
        model.addAttribute("riskStudents", risk.stream().limit(5).toList());
        model.addAttribute("d", dashboardService.build());
        return "index";
    }
}