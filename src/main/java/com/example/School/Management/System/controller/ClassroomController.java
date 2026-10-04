package com.example.School.Management.System.controller;

import com.example.School.Management.System.dto.ClassroomDto;
import com.example.School.Management.System.service.ClassroomService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/classrooms")
public class ClassroomController {

    private final ClassroomService classroomService;

    public ClassroomController(ClassroomService classroomService) {
        this.classroomService = classroomService;
    }

    // List all classrooms -> returns templates/classroom/list.html
    @GetMapping
    public String listClassrooms(@PageableDefault(size = 10, sort = "name") Pageable pageable, Model model) {
        Page<ClassroomDto> classroomPage = classroomService.getAllClassrooms(pageable);
        model.addAttribute("classrooms", classroomPage);
        return "classroom/list";
    }

    // Show form for creating a new classroom -> returns templates/classroom/form.html
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("classroomDto", new ClassroomDto(null, "", null, null));
        return "classroom/form";
    }

    // Handle create submission
    @PostMapping
    public String createClassroom(@Valid @ModelAttribute("classroomDto") ClassroomDto classroomDto,
                                  BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "classroom/form";
        }
        classroomService.createClassroom(classroomDto);
        return "redirect:/classrooms";
    }

    // Show form for editing an existing classroom
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        ClassroomDto classroomDto = classroomService.getClassroomById(id);
        model.addAttribute("classroomDto", classroomDto);
        return "classroom/form";
    }

    // Handle update submission
    @PostMapping("/update/{id}")
    public String updateClassroom(@PathVariable Long id,
                                  @Valid @ModelAttribute("classroomDto") ClassroomDto classroomDto,
                                  BindingResult result) {
        if (result.hasErrors()) {
            return "classroom/form";
        }
        classroomService.updateClassroom(id, classroomDto);
        return "redirect:/classrooms";
    }

    // Delete classroom
    @GetMapping("/delete/{id}")
    public String deleteClassroom(@PathVariable Long id) {
        classroomService.deleteClassroom(id);
        return "redirect:/classrooms";
    }
}
