package com.example.School.Management.System.controller;

import com.example.School.Management.System.dto.ImportResult;
import com.example.School.Management.System.service.NotificationService;
import com.example.School.Management.System.service.StudentImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.security.Principal;

@Controller
@RequestMapping("/admin/import/students")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class StudentImportController {

    private final StudentImportService importService;
    private final NotificationService notifications;

    @GetMapping
    public String form() {
        return "admin/student-import";
    }

    @PostMapping
    public String upload(@RequestParam("file") MultipartFile file, Model model, Authentication authentication){
        if (file.isEmpty()) {
            model.addAttribute("fileError", "import.error.noFile");
            return "admin/student-import";
        }
        try {
            ImportResult result = importService.importFile(file);
            model.addAttribute("result", result);
            if (result.created() > 0) {                                           // NUEVO
                notifications.onStudentsImported(result.total(), result.created(),
                        result.errors().size(), authentication.getName());
            }
        } catch (IllegalArgumentException ex) {
            model.addAttribute("fileError", ex.getMessage());
        } catch (Exception ex) {
            model.addAttribute("fileError", "import.error.unreadable");
        }
        return "admin/student-import";
    }

    @GetMapping("/template.csv")
    public ResponseEntity<byte[]> template() {
        String csv = "\uFEFFfirstName;lastName;email;phone;city;country;birthDate;gender;classroom\n"
                + "Ahmed;Benali;ahmed.benali@mail.com;+212600000000;Tetouan;Morocco;2010-05-21;MALE;\n";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=students-template.csv")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csv.getBytes(StandardCharsets.UTF_8));
    }
}