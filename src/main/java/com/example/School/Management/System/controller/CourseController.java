package com.example.School.Management.System.controller;

import com.example.School.Management.System.dto.CourseDto;
import com.example.School.Management.System.service.CourseService;
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
@RequestMapping("/courses")
public class CourseController {

    private final CourseService courseService;
    private final TeacherService teacherService;

    public CourseController(CourseService courseService, TeacherService teacherService) {
        this.courseService = courseService;
        this.teacherService = teacherService;
    }

    @GetMapping
    public String listCourses(@RequestParam(required = false) String keyword,
                              @PageableDefault(size = 10, sort = "title") Pageable pageable,
                              Model model) {
        Page<CourseDto> coursePage;
        if (keyword != null && !keyword.isEmpty()) {
            coursePage = courseService.searchCourses(keyword, pageable);
            model.addAttribute("keyword", keyword);
        } else {
            coursePage = courseService.getAllCourses(pageable);
        }
        model.addAttribute("courses", coursePage);
        return "course/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("courseDto", new CourseDto(null, "", "", 3, null,null));
        model.addAttribute("teachers", teacherService.getAllTeachers(Pageable.unpaged()).getContent());
        return "course/form";
    }

    @PostMapping
    public String createCourse(@Valid @ModelAttribute("courseDto") CourseDto courseDto,
                               BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("teachers", teacherService.getAllTeachers(Pageable.unpaged()).getContent());
            return "course/form";
        }
        courseService.createCourse(courseDto);
        return "redirect:/courses";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        CourseDto courseDto = courseService.getCourseById(id);
        model.addAttribute("courseDto", courseDto);
        model.addAttribute("teachers", teacherService.getAllTeachers(Pageable.unpaged()).getContent());
        return "course/form";
    }

    @PostMapping("/update/{id}")
    public String updateCourse(@PathVariable Long id,
                               @Valid @ModelAttribute("courseDto") CourseDto courseDto,
                               BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("teachers", teacherService.getAllTeachers(Pageable.unpaged()).getContent());
            return "course/form";
        }
        courseService.updateCourse(id, courseDto);
        return "redirect:/courses";
    }

    @GetMapping("/delete/{id}")
    public String deleteCourse(@PathVariable Long id) {
        courseService.deleteCourse(id);
        return "redirect:/courses";
    }
}