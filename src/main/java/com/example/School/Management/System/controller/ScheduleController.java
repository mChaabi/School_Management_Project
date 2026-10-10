package com.example.School.Management.System.controller;

import com.example.School.Management.System.dto.ScheduleDto;
import com.example.School.Management.System.repository.ClassroomRepository;
import com.example.School.Management.System.repository.CourseRepository;
import com.example.School.Management.System.repository.TeacherRepository;
import com.example.School.Management.System.service.ScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.DayOfWeek;
import java.util.List;

@Controller
@RequestMapping("/schedules")
@RequiredArgsConstructor
public class ScheduleController {

    private static final List<DayOfWeek> DAYS = List.of(
            DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY);

    private final ScheduleService service;
    private final CourseRepository courseRepo;
    private final ClassroomRepository classroomRepo;
    private final TeacherRepository teacherRepo;

    // ---------- weekly grid ----------
    @GetMapping
    public String grid(@RequestParam(required = false) Long classroomId,
                       @RequestParam(required = false) Long teacherId,
                       Model model) {
        model.addAttribute("days", DAYS);
        model.addAttribute("grid", service.grid(classroomId, teacherId));
        model.addAttribute("classrooms", classroomRepo.findAll());
        model.addAttribute("teachers", teacherRepo.findAll());
        model.addAttribute("classroomId", classroomId);
        model.addAttribute("teacherId", teacherId);
        return "schedule/list";
    }

    // ---------- create ----------
    @GetMapping("/new")
    public String create(Model model) {
        model.addAttribute("scheduleDto", new ScheduleDto());
        fillForm(model);
        return "schedule/form";
    }

    @PostMapping
    public String save(@Valid @ModelAttribute("scheduleDto") ScheduleDto dto,
                       BindingResult result, Model model, RedirectAttributes ra) {
        return persist(null, dto, result, model, ra, "Lesson added.");
    }

    // ---------- edit ----------
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model) {
        model.addAttribute("scheduleDto", service.findDto(id));
        fillForm(model);
        return "schedule/form";
    }

    @PostMapping("/update/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("scheduleDto") ScheduleDto dto,
                         BindingResult result, Model model, RedirectAttributes ra) {
        dto.setId(id);
        return persist(id, dto, result, model, ra, "Lesson updated.");
    }

    // ---------- delete ----------
    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        service.delete(id);
        ra.addFlashAttribute("success", "Lesson deleted.");
        return "redirect:/schedules";
    }

    // ---------- helpers ----------
    private String persist(Long id, ScheduleDto dto, BindingResult result,
                           Model model, RedirectAttributes ra, String okMessage) {
        if (!result.hasErrors()) {
            try {
                service.save(id, dto.getCourseId(), dto.getClassroomId(),
                        dto.getDayOfWeek(), dto.getStartTime(), dto.getEndTime());
                ra.addFlashAttribute("success", okMessage);
                return "redirect:/schedules?classroomId=" + dto.getClassroomId();
            } catch (IllegalArgumentException | IllegalStateException e) {
                result.reject("schedule.conflict", e.getMessage());
            }
        }
        fillForm(model);
        return "schedule/form"; // <-- Change from "schedules/form" to "schedule/form"
    }

    private void fillForm(Model model) {
        model.addAttribute("courses", courseRepo.findAll());
        model.addAttribute("classrooms", classroomRepo.findAll());
        model.addAttribute("days", DAYS);
    }
}