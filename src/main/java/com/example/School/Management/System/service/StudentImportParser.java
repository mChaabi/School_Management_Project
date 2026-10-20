package com.example.School.Management.System.service;

import com.example.School.Management.System.dto.ImportRow;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class StudentImportParser {

    private static final List<String> REQUIRED = List.of("firstname", "lastname", "email");

    public List<ImportRow> parse(MultipartFile file) throws IOException {
        String name = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase();
        if (name.endsWith(".csv"))  return parseCsv(file);
        if (name.endsWith(".xlsx")) return parseXlsx(file);
        throw new IllegalArgumentException("import.error.format");
    }

    // ---------- CSV ----------
    private List<ImportRow> parseCsv(MultipartFile file) throws IOException {
        String text = new String(file.getBytes(), StandardCharsets.UTF_8);
        if (text.startsWith("\uFEFF")) text = text.substring(1);        // quita el BOM de Excel

        String firstLine = text.lines().findFirst().orElse("");
        char delimiter = firstLine.contains(";") ? ';' : ',';           // Excel en español usa ;

        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setDelimiter(delimiter)
                .setHeader().setSkipHeaderRecord(true)
                .setIgnoreHeaderCase(true)
                .setIgnoreEmptyLines(true)
                .setTrim(true)
                .build();

        List<ImportRow> rows = new ArrayList<>();
        try (CSVParser parser = CSVParser.parse(text, format)) {
            Set<String> headers = parser.getHeaderMap().keySet().stream()
                    .map(h -> h.toLowerCase().trim()).collect(Collectors.toSet());
            checkHeaders(headers);

            for (CSVRecord r : parser) {
                rows.add(new ImportRow((int) r.getRecordNumber() + 1,
                        csv(r, "firstName"), csv(r, "lastName"), csv(r, "email"),
                        csv(r, "phone"), csv(r, "city"), csv(r, "country"),
                        csv(r, "birthDate"), csv(r, "gender"), csv(r, "classroom")));
            }
        }
        return rows;
    }

    private String csv(CSVRecord r, String col) {
        return r.isMapped(col) && r.isSet(col) ? r.get(col).trim() : "";
    }

    // ---------- XLSX ----------
    private List<ImportRow> parseXlsx(MultipartFile file) throws IOException {
        List<ImportRow> rows = new ArrayList<>();
        try (Workbook wb = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = wb.getSheetAt(0);
            Row header = sheet.getRow(0);
            if (header == null) throw new IllegalArgumentException("import.error.empty");

            DataFormatter fmt = new DataFormatter();
            Map<String, Integer> idx = new HashMap<>();
            for (Cell c : header) {
                idx.put(fmt.formatCellValue(c).trim().toLowerCase(), c.getColumnIndex());
            }
            checkHeaders(idx.keySet());

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                ImportRow r = new ImportRow(i + 1,
                        cell(row, idx, "firstname", fmt), cell(row, idx, "lastname", fmt),
                        cell(row, idx, "email", fmt), cell(row, idx, "phone", fmt),
                        cell(row, idx, "city", fmt), cell(row, idx, "country", fmt),
                        cell(row, idx, "birthdate", fmt), cell(row, idx, "gender", fmt),
                        cell(row, idx, "classroom", fmt));
                if (!isBlankRow(r)) rows.add(r);
            }
        }
        return rows;
    }

    private String cell(Row row, Map<String, Integer> idx, String col, DataFormatter fmt) {
        Integer i = idx.get(col);
        if (i == null) return "";
        Cell c = row.getCell(i);
        if (c == null) return "";
        if (c.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(c)) {
            return c.getLocalDateTimeCellValue().toLocalDate().toString();   // yyyy-MM-dd
        }
        return fmt.formatCellValue(c).trim();
    }

    // ---------- comunes ----------
    private void checkHeaders(Set<String> headers) {
        if (!headers.containsAll(REQUIRED)) {
            throw new IllegalArgumentException("import.error.headers");
        }
    }

    private boolean isBlankRow(ImportRow r) {
        return r.firstName().isBlank() && r.lastName().isBlank() && r.email().isBlank();
    }
}
