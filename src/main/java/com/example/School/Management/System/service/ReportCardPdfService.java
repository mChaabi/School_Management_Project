package com.example.School.Management.System.service;

import com.example.School.Management.System.dto.StudentCourseRow;
import com.example.School.Management.System.dto.StudentDetailDto;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class ReportCardPdfService {

    private static final Color DARK    = new Color(15, 23, 42);
    private static final Color PRIMARY = new Color(79, 70, 229);
    private static final Color MUTED   = new Color(100, 116, 139);
    private static final Color LIGHT   = new Color(241, 245, 249);

    private static final Font TITLE = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, DARK);
    private static final Font SUB   = FontFactory.getFont(FontFactory.HELVETICA, 10, MUTED);
    private static final Font H2    = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, PRIMARY);
    private static final Font BODY  = FontFactory.getFont(FontFactory.HELVETICA, 10, DARK);
    private static final Font BOLD  = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, DARK);
    private static final Font TH    = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);

    private final StudentDetailService detailService;

    public byte[] generate(Long studentId) {
        // also checks that the logged-in user may see this student
        StudentDetailDto s = detailService.getDetail(studentId);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4, 40, 40, 50, 50);
            PdfWriter.getInstance(doc, out);
            doc.open();

            // ----- Header -----
            Paragraph school = new Paragraph("EduManage", TITLE);
            doc.add(school);
            doc.add(new Paragraph("Official report card  |  Issued on "
                    + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), SUB));
            doc.add(Chunk.NEWLINE);

            // ----- Student info -----
            doc.add(new Paragraph("STUDENT", H2));
            PdfPTable info = new PdfPTable(2);
            info.setWidthPercentage(100);
            info.setSpacingBefore(6);
            info.setSpacingAfter(14);
            infoRow(info, "Name", s.fullName());
            infoRow(info, "Email", s.email());
            infoRow(info, "Classroom", s.classroomName() != null ? s.classroomName() : "Not assigned");
            infoRow(info, "Birth date", s.birthDate() != null
                    ? s.birthDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "-");
            doc.add(info);

            // ----- Grades table -----
            doc.add(new Paragraph("GRADES", H2));
            PdfPTable table = new PdfPTable(new float[]{3.2f, 1f, 2.8f, 1.3f, 1.8f});
            table.setWidthPercentage(100);
            table.setSpacingBefore(6);
            table.setHeaderRows(1);

            for (String h : new String[]{"Course", "Credits", "Teacher", "Grade /20", "Remark"}) {
                PdfPCell c = new PdfPCell(new Phrase(h, TH));
                c.setBackgroundColor(PRIMARY);
                c.setPadding(7);
                c.setBorderColor(PRIMARY);
                table.addCell(c);
            }

            boolean zebra = false;
            for (StudentCourseRow r : s.courses()) {
                Color bg = zebra ? LIGHT : Color.WHITE;
                zebra = !zebra;
                table.addCell(cell(r.courseTitle(), BOLD, bg, Element.ALIGN_LEFT));
                table.addCell(cell(r.credits() != null ? r.credits().toString() : "-", BODY, bg, Element.ALIGN_CENTER));
                table.addCell(cell(r.teacherName(), BODY, bg, Element.ALIGN_LEFT));
                table.addCell(cell(r.grade() != null ? String.format("%.2f", r.grade()) : "-", BOLD, bg, Element.ALIGN_CENTER));
                table.addCell(cell(remark(r.grade()), BODY, bg, Element.ALIGN_LEFT));
            }
            if (s.courses().isEmpty()) {
                PdfPCell empty = cell("Not enrolled in any course.", SUB, Color.WHITE, Element.ALIGN_CENTER);
                empty.setColspan(5);
                empty.setPadding(14);
                table.addCell(empty);
            }
            doc.add(table);

            // ----- Average -----
            doc.add(Chunk.NEWLINE);
            PdfPTable summary = new PdfPTable(2);
            summary.setWidthPercentage(60);
            summary.setHorizontalAlignment(Element.ALIGN_RIGHT);
            summaryRow(summary, "General average",
                    s.average() != null ? String.format("%.2f / 20", s.average()) : "-");
            summaryRow(summary, "Overall remark", remark(s.average()));
            doc.add(summary);

            // ----- Signatures -----
            doc.add(Chunk.NEWLINE);
            doc.add(Chunk.NEWLINE);
            doc.add(Chunk.NEWLINE);
            PdfPTable sign = new PdfPTable(2);
            sign.setWidthPercentage(100);
            sign.addCell(noBorder("Principal's signature", SUB));
            sign.addCell(noBorder("Parent's signature", SUB));
            doc.add(sign);

            doc.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("Could not generate the report card", e);
        }
    }

    // ---------- helpers ----------

    private String remark(Double g) {
        if (g == null) return "-";
        if (g >= 16) return "Excellent";
        if (g >= 14) return "Very good";
        if (g >= 12) return "Good";
        if (g >= 10) return "Pass";
        return "Needs improvement";
    }

    private PdfPCell cell(String text, Font font, Color bg, int align) {
        PdfPCell c = new PdfPCell(new Phrase(text, font));
        c.setBackgroundColor(bg);
        c.setHorizontalAlignment(align);
        c.setPadding(7);
        c.setBorderColor(new Color(226, 232, 240));
        return c;
    }

    private void infoRow(PdfPTable t, String label, String value) {
        PdfPCell l = new PdfPCell(new Phrase(label, SUB));
        PdfPCell v = new PdfPCell(new Phrase(value, BOLD));
        for (PdfPCell c : new PdfPCell[]{l, v}) {
            c.setBorder(Rectangle.BOTTOM);
            c.setBorderColor(new Color(226, 232, 240));
            c.setPadding(6);
        }
        t.addCell(l);
        t.addCell(v);
    }

    private void summaryRow(PdfPTable t, String label, String value) {
        PdfPCell l = cell(label, BOLD, LIGHT, Element.ALIGN_LEFT);
        PdfPCell v = cell(value, BOLD, LIGHT, Element.ALIGN_RIGHT);
        t.addCell(l);
        t.addCell(v);
    }

    private PdfPCell noBorder(String text, Font f) {
        PdfPCell c = new PdfPCell(new Phrase("______________________\n" + text, f));
        c.setBorder(Rectangle.NO_BORDER);
        c.setHorizontalAlignment(Element.ALIGN_CENTER);
        return c;
    }
}