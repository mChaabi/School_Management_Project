package com.example.School.Management.System.service;

import com.example.School.Management.System.dto.ImportResult;
import com.example.School.Management.System.dto.ImportRow;
import com.example.School.Management.System.dto.RowError;
import com.example.School.Management.System.dto.StudentDto;
import com.example.School.Management.System.entity.Classroom;
import com.example.School.Management.System.enums.Gender;
import com.example.School.Management.System.repository.ClassroomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentImportService {

    private static final int MAX_ROWS = 1000;

    private final StudentImportParser parser;
    private final StudentService studentService;
    private final ClassroomRepository classroomRepository;

    public ImportResult importFile(MultipartFile file) throws IOException {
        List<ImportRow> rows = parser.parse(file);
        if (rows.isEmpty()) throw new IllegalArgumentException("import.error.empty");
        if (rows.size() > MAX_ROWS) throw new IllegalArgumentException("import.error.tooMany");

        Map<String, Long> classrooms = classroomRepository.findAll().stream()
                .collect(Collectors.toMap(c -> c.getName().trim().toLowerCase(),
                        Classroom::getId, (a, b) -> a));

        Set<String> seenEmails = new HashSet<>();
        List<RowError> errors = new ArrayList<>();
        int created = 0;

        for (ImportRow r : rows) {
            try {
                if (r.firstName().isBlank() || r.lastName().isBlank() || r.email().isBlank()) {
                    throw new IllegalArgumentException("import.error.required");
                }
                if (!seenEmails.add(r.email().toLowerCase())) {
                    throw new IllegalArgumentException("import.error.dupInFile");
                }

                LocalDate birth = parseDate(r.birthDate());
                Gender gender = parseGender(r.gender());
                Long classroomId = null;
                if (!r.classroom().isBlank()) {
                    classroomId = classrooms.get(r.classroom().toLowerCase());
                    if (classroomId == null) throw new IllegalArgumentException("import.error.classroom");
                }

                studentService.createStudent(new StudentDto(
                        null, r.firstName(), r.lastName(), r.email(),
                        r.phone(), r.city(), r.country(),
                        birth, gender, classroomId, null));
                created++;

            } catch (Exception ex) {
                errors.add(new RowError(r.rowNumber(), ex.getMessage()));
            }
        }
        return new ImportResult(rows.size(), created, errors);
    }

    private LocalDate parseDate(String s) {
        if (s.isBlank()) return null;
        try {
            return LocalDate.parse(s);                                   // 2010-05-21
        } catch (DateTimeParseException e) {
            try {
                return LocalDate.parse(s, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            } catch (DateTimeParseException e2) {
                throw new IllegalArgumentException("import.error.date");
            }
        }
    }

    private Gender parseGender(String s) {
        if (s.isBlank()) return null;
        return switch (s.trim().toUpperCase(Locale.ROOT)) {
            case "MALE", "M"   -> Gender.MALE;
            case "FEMALE", "F" -> Gender.FEMALE;
            default -> throw new IllegalArgumentException("import.error.gender");
        };
    }
}