package com.example.School.Management.System.service;

import com.example.School.Management.System.entity.*;
import com.example.School.Management.System.repository.EnrollmentRepository;
import com.example.School.Management.System.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ExportService {

    public record TableData(String sheetName, List<String> headers, List<List<Object>> rows) {}

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final StudentRepository studentRepo;
    private final EnrollmentRepository enrollmentRepo;

    // ---------- table builders ----------

    @Transactional(readOnly = true)
    public TableData students() {
        List<List<Object>> rows = new ArrayList<>();
        for (Student s : studentRepo.findAllForExport()) {
            rows.add(Arrays.asList(
                    s.getId(), s.getFirstName(), s.getLastName(), s.getEmail(), s.getPhone(),
                    s.getGender() != null ? s.getGender().name() : "",
                    date(s.getBirthDate()),
                    s.getClassroom() != null ? s.getClassroom().getName() : ""));
        }
        return new TableData("Students",
                List.of("ID", "First name", "Last name", "Email", "Phone", "Gender", "Birth date", "Classroom"),
                rows);
    }

    @Transactional(readOnly = true)
    public TableData grades() {
        List<List<Object>> rows = new ArrayList<>();
        for (Enrollment e : enrollmentRepo.findAllForExport()) {
            Student s = e.getStudent();
            Course c = e.getCourse();
            Teacher t = c.getTeacher();
            rows.add(Arrays.asList(
                    s.getFirstName() + " " + s.getLastName(),
                    c.getTitle(),
                    c.getCredits(),
                    t != null ? t.getFirstName() + " " + t.getLastName() : "",
                    date(e.getEnrollmentDate()),
                    e.getGrade()));
        }
        return new TableData("Grades",
                List.of("Student", "Course", "Credits", "Teacher", "Enrolled on", "Grade /20"),
                rows);
    }

    // ---------- writers ----------

    public byte[] toXlsx(TableData t) {
        try (XSSFWorkbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sh = wb.createSheet(t.sheetName());

            Font white = wb.createFont();
            white.setBold(true);
            white.setColor(IndexedColors.WHITE.getIndex());
            CellStyle head = wb.createCellStyle();
            head.setFont(white);
            head.setFillForegroundColor(IndexedColors.INDIGO.getIndex());
            head.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            Row hr = sh.createRow(0);
            for (int i = 0; i < t.headers().size(); i++) {
                Cell c = hr.createCell(i);
                c.setCellValue(t.headers().get(i));
                c.setCellStyle(head);
            }

            int r = 1;
            for (List<Object> row : t.rows()) {
                Row xr = sh.createRow(r++);
                for (int i = 0; i < row.size(); i++) {
                    Object v = row.get(i);
                    Cell c = xr.createCell(i);
                    if (v instanceof Number n) c.setCellValue(n.doubleValue());
                    else c.setCellValue(v == null ? "" : v.toString());
                }
            }

            for (int i = 0; i < t.headers().size(); i++) sh.autoSizeColumn(i);
            sh.createFreezePane(0, 1);
            sh.setAutoFilter(new CellRangeAddress(0, Math.max(r - 1, 0), 0, t.headers().size() - 1));

            wb.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("Could not build the Excel file", e);
        }
    }

    public byte[] toCsv(TableData t) {
        StringBuilder sb = new StringBuilder("\uFEFF");          // BOM so Excel reads UTF-8 accents
        sb.append(line(new ArrayList<>(t.headers())));
        for (List<Object> row : t.rows()) sb.append(line(row));
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    // ---------- helpers ----------

    private String line(List<?> values) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) sb.append(';');                           // ';' opens correctly in French/Spanish Excel
            sb.append(csv(values.get(i)));
        }
        return sb.append("\r\n").toString();
    }

    private String csv(Object v) {
        String s = v == null ? "" : v.toString();
        // neutralise spreadsheet formula injection (=, +, -, @ at the start)
        if (!s.isEmpty() && "=+-@".indexOf(s.charAt(0)) >= 0 && !(v instanceof Number)) s = "'" + s;
        if (s.contains(";") || s.contains("\"") || s.contains("\n"))
            s = "\"" + s.replace("\"", "\"\"") + "\"";
        return s;
    }

    private String date(LocalDate d) {
        return d == null ? "" : d.format(DATE);
    }
}