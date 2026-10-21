package com.example.School.Management.System.service;

import com.example.School.Management.System.entity.Fee;
import com.example.School.Management.System.entity.Student;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.awt.Rectangle;
import java.io.ByteArrayOutputStream;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class FeeReceiptPdfService {

    private static final Color DARK    = new Color(15, 23, 42);
    private static final Color PRIMARY = new Color(79, 70, 229);
    private static final Color MUTED   = new Color(100, 116, 139);
    private static final Color LIGHT   = new Color(241, 245, 249);

    private static final com.lowagie.text.Font TITLE  = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, DARK);
    private static final com.lowagie.text.Font SUB    = FontFactory.getFont(FontFactory.HELVETICA, 10, MUTED);
    private static final com.lowagie.text.Font H2     = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, PRIMARY);
    private static final com.lowagie.text.Font BOLD   = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, DARK);
    private static final Font AMOUNT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, PRIMARY);

    @Value("${school.currency:MAD}")
    private String currency;

    public byte[] generate(Fee fee) {
        Student s = fee.getStudent();
        DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4, 40, 40, 50, 50);
            PdfWriter.getInstance(doc, out);
            doc.open();

            doc.add(new Paragraph("EduManage", TITLE));
            doc.add(new Paragraph("Payment receipt  |  No. " + fee.getReceiptNumber(), SUB));
            doc.add(Chunk.NEWLINE);

            doc.add(new Paragraph("DETAILS", H2));
            PdfPTable info = new PdfPTable(2);
            info.setWidthPercentage(100);
            info.setSpacingBefore(6);
            info.setSpacingAfter(14);
            row(info, "Student", s.getFirstName() + " " + s.getLastName());
            row(info, "Concept", fee.getTitle());
            row(info, "Due date", fee.getDueDate().format(df));
            row(info, "Paid on", fee.getPaidDate().format(df));
            row(info, "Payment method", fee.getMethod() != null ? fee.getMethod().name() : "-");
            doc.add(info);

            PdfPTable box = new PdfPTable(2);
            box.setWidthPercentage(70);
            box.setHorizontalAlignment(Element.ALIGN_RIGHT);
            PdfPCell l = new PdfPCell(new Phrase("AMOUNT PAID", BOLD));
            PdfPCell v = new PdfPCell(new Phrase(fee.getAmount().setScale(2, RoundingMode.HALF_UP)
                    .toPlainString() + " " + currency, AMOUNT));
            for (PdfPCell c : new PdfPCell[]{l, v}) {
                c.setBackgroundColor(LIGHT);
                c.setBorderColor(new Color(226, 232, 240));
                c.setPadding(12);
                c.setVerticalAlignment(Element.ALIGN_MIDDLE);
            }
            v.setHorizontalAlignment(Element.ALIGN_RIGHT);
            box.addCell(l);
            box.addCell(v);
            doc.add(box);

            doc.add(Chunk.NEWLINE);
            doc.add(Chunk.NEWLINE);
            doc.add(Chunk.NEWLINE);
            PdfPTable sign = new PdfPTable(1);
            sign.setWidthPercentage(40);
            sign.setHorizontalAlignment(Element.ALIGN_RIGHT);
            PdfPCell sc = new PdfPCell(new Phrase("______________________\nAuthorized signature", SUB));
            sc.setBorder(PdfPCell.NO_BORDER);
            sc.setHorizontalAlignment(Element.ALIGN_CENTER);
            sign.addCell(sc);
            doc.add(sign);

            doc.add(new Paragraph("Issued on " + LocalDate.now().format(df), SUB));
            doc.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("Could not generate the receipt", e);
        }
    }

    private void row(PdfPTable t, String label, String value) {
        PdfPCell l = new PdfPCell(new Phrase(label, SUB));
        PdfPCell v = new PdfPCell(new Phrase(value, BOLD));
        for (PdfPCell c : new PdfPCell[]{l, v}) {
            c.setBorder(PdfPCell.BOTTOM);
            c.setBorderColor(new Color(226, 232, 240));
            c.setPadding(6);
        }
        t.addCell(l);
        t.addCell(v);
    }
}
