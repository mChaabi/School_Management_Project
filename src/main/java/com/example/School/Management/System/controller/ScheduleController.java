package com.example.School.Management.System.controller;

import com.example.School.Management.System.dto.ScheduleDto;
import com.example.School.Management.System.exception.ScheduleConflictException;
import com.example.School.Management.System.repository.ClassroomRepository;
import com.example.School.Management.System.repository.CourseRepository;
import com.example.School.Management.System.repository.TeacherRepository;
import com.example.School.Management.System.service.NotificationService;
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
    private final NotificationService notificationService;

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
    // ---------- create form handler ----------
    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("scheduleDto", new ScheduleDto());
        fillForm(model);
        return "schedule/form";
    }

    // ---------- save (create) ----------
    @PostMapping
    public String save(@Valid @ModelAttribute("scheduleDto") ScheduleDto dto,
                       BindingResult result, Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            fillForm(model);
            return "schedule/form";
        }
        try {
            service.create(dto); // or service.save(...) depending on your service method name
            triggerNotification(dto, "added");
            ra.addFlashAttribute("success", "Lesson added.");
            return "redirect:/schedules?classroomId=" + dto.getClassroomId();
        } catch (ScheduleConflictException ex) {
            result.reject(ex.getMessage()); // i18n key as global error
            fillForm(model);
            return "schedule/form";
        }
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
        if (result.hasErrors()) {
            fillForm(model);
            return "schedule/form";
        }
        try {
            service.update(id, dto);
            triggerNotification(dto, "updated");
            ra.addFlashAttribute("success", "Lesson updated.");
            return "redirect:/schedules?classroomId=" + dto.getClassroomId();
        } catch (ScheduleConflictException ex) {
            result.reject(ex.getMessage()); // i18n key as global error
            fillForm(model);
            return "schedule/form";
        }
    }

    // ---------- delete ----------
    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        service.delete(id);
        ra.addFlashAttribute("success", "Lesson deleted.");
        return "redirect:/schedules";
    }

    // ---------- helpers ----------
    private void triggerNotification(ScheduleDto dto, String action) {
        var classroom = classroomRepo.findById(dto.getClassroomId()).orElse(null);
        String classroomName = (classroom != null) ? classroom.getName() : "Unknown";

        notificationService.onScheduleChanged(
                dto.getCourseId(),
                dto.getClassroomId(),
                classroomName,
                action,
                dto.getDayOfWeek().name(),
                dto.getStartTime().toString()
        );
    }

    private void fillForm(Model model) {
        model.addAttribute("courses", courseRepo.findAll());
        model.addAttribute("classrooms", classroomRepo.findAll());
        model.addAttribute("days", DAYS);
    }
}