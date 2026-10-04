package com.example.School.Management.System.controller;

import com.example.School.Management.System.dto.AttendanceSheetDto;
import com.example.School.Management.System.enums.AttendanceStatus;
import com.example.School.Management.System.service.AttendanceService;
import com.example.School.Management.System.service.ClassroomService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequestMapping("/attendances")
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final ClassroomService classroomService;

    public AttendanceController(AttendanceService attendanceService, ClassroomService classroomService) {
        this.attendanceService = attendanceService;
        this.classroomService = classroomService;
    }

    // Attendance sheet (pick classroom + date) -> templates/attendance/sheet.html
    @GetMapping
    public String showSheet(@RequestParam(required = false) Long classroomId,
                            @RequestParam(required = false)
                            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                            Model model) {
        LocalDate day = (date != null) ? date : LocalDate.now();
        AttendanceSheetDto sheet = (classroomId != null)
                ? attendanceService.getSheet(classroomId, day)
                : new AttendanceSheetDto();
        model.addAttribute("sheet", sheet);
        addSheetLookups(model);
        return "attendance/sheet";
    }

    // Save the whole sheet (create or update every row)
    @PostMapping("/save")
    public String saveSheet(@Valid @ModelAttribute("sheet") AttendanceSheetDto sheet,
                            BindingResult result, Model model, RedirectAttributes redirect) {
        if (result.hasErrors()) {
            addSheetLookups(model);
            return "attendance/sheet";
        }
        attendanceService.saveSheet(sheet);
        redirect.addFlashAttribute("message", "Attendance saved successfully");
        return "redirect:/attendances?classroomId=" + sheet.getClassroomId() + "&date=" + sheet.getDate();
    }

    // Student history + summary -> templates/attendance/history.html
    @GetMapping("/student/{studentId}")
    public String studentHistory(@PathVariable Long studentId, Model model) {
        model.addAttribute("studentId", studentId);
        model.addAttribute("attendances", attendanceService.findByStudentId(studentId));
        model.addAttribute("summary", attendanceService.getSummary(studentId));
        return "attendance/history";
    }

    // Delete one attendance record
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id, @RequestParam Long studentId) {
        attendanceService.delete(id);
        return "redirect:/attendances/student/" + studentId;
    }

    private void addSheetLookups(Model model) {
        model.addAttribute("classrooms", classroomService.getAllClassrooms(Pageable.unpaged()).getContent());
        model.addAttribute("statuses", AttendanceStatus.values());
    }
}