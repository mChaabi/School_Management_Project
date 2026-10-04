package com.example.School.Management.System.controller;

import com.example.School.Management.System.dto.TeacherDto;
import com.example.School.Management.System.service.TeacherService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/teachers")
public class TeacherController {

    private final TeacherService teacherService;

    public TeacherController(TeacherService teacherService) {
        this.teacherService = teacherService;
    }

    @GetMapping
    public String listTeachers(@RequestParam(required = false) String keyword,
                               @PageableDefault(size = 10, sort = "lastName") Pageable pageable,
                               Model model) {
        Page<TeacherDto> teacherPage;
        if (keyword != null && !keyword.isEmpty()) {
            teacherPage = teacherService.searchTeachers(keyword, pageable);
            model.addAttribute("keyword", keyword);
        } else {
            teacherPage = teacherService.getAllTeachers(pageable);
        }
        model.addAttribute("teachers", teacherPage);
        return "teacher/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("teacherDto", new TeacherDto(null, "", "", "", "", "", null));
        return "teacher/form";
    }

    @PostMapping
    public String createTeacher(@Valid @ModelAttribute("teacherDto") TeacherDto teacherDto,
                                BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "teacher/form";
        }
        teacherService.createTeacher(teacherDto);
        return "redirect:/teachers";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        TeacherDto teacherDto = teacherService.getTeacherById(id);
        model.addAttribute("teacherDto", teacherDto);
        return "teacher/form";
    }

    @PostMapping("/update/{id}")
    public String updateTeacher(@PathVariable Long id,
                                @Valid @ModelAttribute("teacherDto") TeacherDto teacherDto,
                                BindingResult result) {
        if (result.hasErrors()) {
            return "teacher/form";
        }
        teacherService.updateTeacher(id, teacherDto);
        return "redirect:/teachers";
    }

    @GetMapping("/delete/{id}")
    public String deleteTeacher(@PathVariable Long id) {
        teacherService.deleteTeacher(id);
        return "redirect:/teachers";
    }
}