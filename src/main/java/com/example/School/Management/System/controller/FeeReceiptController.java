package com.example.School.Management.System.controller;

import com.example.School.Management.System.entity.Fee;
import com.example.School.Management.System.service.FeeReceiptPdfService;
import com.example.School.Management.System.service.FeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequiredArgsConstructor
public class FeeReceiptController {

    private final FeeService feeService;
    private final FeeReceiptPdfService pdfService;

    @GetMapping("/fees/{id}/receipt")
    public ResponseEntity<byte[]> receipt(@PathVariable Long id, Authentication auth) {
        Fee fee = feeService.getPaidFeeForReceipt(id, auth);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=" + fee.getReceiptNumber() + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfService.generate(fee));
    }
}
