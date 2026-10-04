package com.example.School.Management.System.controller;

import com.example.School.Management.System.dto.StudentDto;
import com.example.School.Management.System.service.ClassroomService;
import com.example.School.Management.System.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/students")
public class StudentController {

    private final StudentService studentService;
    private final ClassroomService classroomService;

    public StudentController(StudentService studentService, ClassroomService classroomService) {
        this.studentService = studentService;
        this.classroomService = classroomService;
    }

    // List all students -> returns templates/student/list.html
    @GetMapping
    public String listStudents(@RequestParam(required = false) String keyword,
                               @PageableDefault(size = 10, sort = "lastName") Pageable pageable,
                               Model model) {
        Page<StudentDto> studentPage;
        if (keyword != null && !keyword.isEmpty()) {
            studentPage = studentService.searchStudents(keyword, pageable);
            model.addAttribute("keyword", keyword);
        } else {
            studentPage = studentService.getAllStudents(pageable);
        }
        model.addAttribute("students", studentPage);
        return "student/list";
    }

    // Show form for creating a new student -> returns templates/student/form.html
    @GetMapping("/new")
    public String showCreateForm(Model model, Pageable pageable) {
        model.addAttribute("studentDto", new StudentDto(null, "", "", "", "", null, null, null));
        model.addAttribute("classrooms", classroomService.getAllClassrooms(Pageable.unpaged()).getContent());
        return "student/form";
    }

    // Handle create submission
    @PostMapping
    public String createStudent(@Valid @ModelAttribute("studentDto") StudentDto studentDto,
                                BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("classrooms", classroomService.getAllClassrooms(Pageable.unpaged()).getContent());
            return "student/form";
        }
        studentService.createStudent(studentDto);
        return "redirect:/students";
    }

    // Show form for editing an existing student
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        StudentDto studentDto = studentService.getStudentById(id);
        model.addAttribute("studentDto", studentDto);
        model.addAttribute("classrooms", classroomService.getAllClassrooms(Pageable.unpaged()).getContent());
        return "student/form";
    }

    // Handle update submission
    @PostMapping("/update/{id}")
    public String updateStudent(@PathVariable Long id,
                                @Valid @ModelAttribute("studentDto") StudentDto studentDto,
                                BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("classrooms", classroomService.getAllClassrooms(Pageable.unpaged()).getContent());
            return "student/form";
        }
        studentService.updateStudent(id, studentDto);
        return "redirect:/students";
    }

    // Delete student
    @GetMapping("/delete/{id}")
    public String deleteStudent(@PathVariable Long id) {
        studentService.deleteStudent(id);
        return "redirect:/students";
    }
}
