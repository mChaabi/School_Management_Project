package com.example.School.Management.System.controller;

import com.example.School.Management.System.service.ExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.nio.charset.StandardCharsets;

@Controller
@RequestMapping("/export")
@RequiredArgsConstructor
public class ExportController {

    private static final MediaType XLSX =
            MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    private static final MediaType CSV = new MediaType("text", "csv", StandardCharsets.UTF_8);

    private final ExportService service;

    @GetMapping("/students.xlsx")
    public ResponseEntity<byte[]> studentsXlsx() {
        return file(service.toXlsx(service.students()), "students.xlsx", XLSX);
    }

    @GetMapping("/students.csv")
    public ResponseEntity<byte[]> studentsCsv() {
        return file(service.toCsv(service.students()), "students.csv", CSV);
    }

    @GetMapping("/grades.xlsx")
    public ResponseEntity<byte[]> gradesXlsx() {
        return file(service.toXlsx(service.grades()), "grades.xlsx", XLSX);
    }

    @GetMapping("/grades.csv")
    public ResponseEntity<byte[]> gradesCsv() {
        return file(service.toCsv(service.grades()), "grades.csv", CSV);
    }

    private ResponseEntity<byte[]> file(byte[] data, String name, MediaType type) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(name).build().toString())
                .contentType(type)
                .body(data);
    }
}