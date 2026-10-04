package com.example.School.Management.System.controller;

import com.example.School.Management.System.dto.EnrollmentDto;
import com.example.School.Management.System.service.CourseService;
import com.example.School.Management.System.service.EnrollmentService;
import com.example.School.Management.System.service.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;
    private final StudentService studentService;
    private final CourseService courseService;

    private void addFormData(Model model) {
        model.addAttribute("students", studentService.getAllStudents(Pageable.unpaged()).getContent());
        model.addAttribute("courses", courseService.getAllCourses(Pageable.unpaged()).getContent());
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("enrollmentDto", new EnrollmentDto(null, null, null, null, null));
        addFormData(model);
        return "enrollments/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("enrollmentDto") EnrollmentDto dto,
                         BindingResult result, Model model) {
        if (result.hasErrors()) {
            addFormData(model);          // required, otherwise the page crashes
            return "enrollments/form";
        }
        enrollmentService.create(dto);
        return "redirect:/enrollments";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        model.addAttribute("enrollmentDto", enrollmentService.findById(id));
        addFormData(model);
        return "enrollments/form";
    }

    @PostMapping("/update/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("enrollmentDto") EnrollmentDto dto,
                         BindingResult result, Model model) {
        if (result.hasErrors()) {
            addFormData(model);
            return "enrollments/form";
        }
        enrollmentService.update(id, dto);
        return "redirect:/enrollments";
    }
    // findAll, findById (detail) and delete stay as they are (delete → @PostMapping)
}