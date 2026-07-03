package com.whylock.whylock.controller;

import com.whylock.whylock.model.AiScanResponse;
import com.whylock.whylock.service.PdfReportService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/report")
public class ReportController {

    private final PdfReportService pdfReportService;

    public ReportController(
            PdfReportService pdfReportService
    ) {
        this.pdfReportService = pdfReportService;
    }

    @PostMapping("/generate")
    public ResponseEntity<byte[]> generate(
            @RequestBody AiScanResponse response
    ) {

        byte[] pdf =
                pdfReportService.generateReport(response);

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=WHYLOCK_REPORT.pdf"
                )
                .contentType(
                        MediaType.APPLICATION_PDF
                )
                .body(pdf);
    }
}