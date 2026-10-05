package com.example.School.Management.System.api;

import com.example.School.Management.System.dto.AttendanceSheetDto;
import com.example.School.Management.System.service.AttendanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/attendances")
@RequiredArgsConstructor
public class AttendanceRestController {

    private final AttendanceService attendanceService;

    // GET /api/attendances/sheet?classroomId=1&date=2026-10-05
    @GetMapping("/sheet")
    public AttendanceSheetDto getSheet(@RequestParam Long classroomId,
                                       @RequestParam(required = false)
                                       @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate day = (date != null) ? date : LocalDate.now();
        return attendanceService.getSheet(classroomId, day);
    }

    // POST /api/attendances/sheet  (saves every row)
    @PostMapping("/sheet")
    @ResponseStatus(HttpStatus.CREATED)
    public AttendanceSheetDto saveSheet(@Valid @RequestBody AttendanceSheetDto sheet) {
        attendanceService.saveSheet(sheet);
        return attendanceService.getSheet(sheet.getClassroomId(), sheet.getDate());
    }

    @GetMapping("/student/{studentId}")
    public Object history(@PathVariable Long studentId) {
        return attendanceService.findByStudentId(studentId);
    }

    @GetMapping("/student/{studentId}/summary")
    public Object summary(@PathVariable Long studentId) {
        return attendanceService.getSummary(studentId);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        attendanceService.delete(id);
    }
}