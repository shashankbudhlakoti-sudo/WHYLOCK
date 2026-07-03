package com.whylock.whylock.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.whylock.whylock.model.AiScanResponse;
import com.whylock.whylock.model.ScanHistory;
import com.whylock.whylock.model.User;
import com.whylock.whylock.repository.ScanHistoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EnhancedReportService {

    private static final Logger log =
            LoggerFactory.getLogger(EnhancedReportService.class);

    private final EmailService emailService;
    private final ScanHistoryRepository scanHistoryRepository;
    private final PdfReportService pdfReportService;
    private final ObjectMapper objectMapper;

    public EnhancedReportService(
            EmailService emailService,
            ScanHistoryRepository scanHistoryRepository,
            PdfReportService pdfReportService,
            ObjectMapper objectMapper) {

        this.emailService = emailService;
        this.scanHistoryRepository = scanHistoryRepository;
        this.pdfReportService = pdfReportService;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public void sendReportForResponse(User user, AiScanResponse response) {
        try {
            byte[] pdf = pdfReportService.generateReport(response);

            String text = """
                Hello %s,

                Your website security scan has completed successfully.

                Your AI-generated WHYLOCK Security Report is attached.

                Thank you for choosing WHYLOCK.

                Know Why A System Locks Before It Locks

                Regards,
                WHYLOCK Team
                """.formatted(user.getUsername());

            emailService.sendHtmlEmailWithAttachment(
                    user.getEmail(),
                    "🛡 WHYLOCK Security Report",
                    text,
                    "WHYLOCK-Security-Report.pdf",
                    pdf
            );

            log.info("WHYLOCK report (direct) sent successfully to {}", user.getEmail());

        } catch (Exception e) {
            log.error("Failed to send direct WHYLOCK report to {}", user.getEmail(), e);
        }
    }

    @Transactional(readOnly = true)
    public byte[] generatePdfForUser(User user) throws Exception {
        try {
            List<ScanHistory> scans =
                    scanHistoryRepository.findByUserOrderByScannedAtDesc(user);

            AiScanResponse response = null;

            if (scans != null && !scans.isEmpty()) {
                String full = scans.get(0).getFullResponse();

                if (full != null && !full.isBlank()) {
                    try {
                        response = objectMapper.readValue(full, AiScanResponse.class);
                    } catch (Exception ex) {
                        log.warn("Unable to deserialize scan response for {}", user.getUsername(), ex);
                    }
                }
            }

            if (response == null) {
                response = new AiScanResponse();
                response.setUrl("Account report for " + user.getUsername());
                response.setReportId("account-" + user.getId() + "-" + System.currentTimeMillis());
                response.setOverallRisk("UNKNOWN");
                response.setRiskScore(0);
                response.setSummary("No recent scans available for this account.");
                response.setScannedAt(
                        scans != null && !scans.isEmpty()
                                ? scans.get(0).getScannedAt()
                                : LocalDateTime.now()
                );
                response.setAiModel("WHYLOCK");
            }

            return pdfReportService.generateReport(response);

        } catch (Exception e) {
            log.error("Failed to generate PDF for {}", user.getUsername(), e);
            throw e;
        }
    }

    @Transactional
    public void sendReportToUser(User user) {
        try {
            byte[] pdf = generatePdfForUser(user);

            String text = """
                    Hello %s,

                    Your website security scan has completed successfully.

                    Your AI-generated WHYLOCK Security Report is attached.

                    Thank you for choosing WHYLOCK.

                    Know Why A System Locks Before It Locks

                    Regards,
                    WHYLOCK Team
                    """.formatted(user.getUsername());

            emailService.sendHtmlEmailWithAttachment(
                    user.getEmail(),
                    "🛡 WHYLOCK Security Report",
                    text,
                    "WHYLOCK-Security-Report.pdf",
                    pdf
            );

            log.info("WHYLOCK report sent successfully to {}", user.getEmail());

        } catch (Exception e) {
            log.error("Failed to send WHYLOCK report to {}", user.getEmail(), e);
        }
    }

    @Transactional(readOnly = true)
    public ResponseEntity<ByteArrayResource> generateAndDownloadForUser(User user) {
        try {
            byte[] pdf = generatePdfForUser(user);
            ByteArrayResource resource = new ByteArrayResource(pdf);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("inline", "WHYLOCK-Security-Report.pdf");

            return new ResponseEntity<>(resource, headers, HttpStatus.OK);

        } catch (Exception e) {
            log.error("Failed to generate download for {}", user.getUsername(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
