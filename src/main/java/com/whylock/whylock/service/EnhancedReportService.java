package com.whylock.whylock.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.whylock.whylock.model.AiScanResponse;
import com.whylock.whylock.model.ScanHistory;
import com.whylock.whylock.model.User;
import com.whylock.whylock.repository.ScanHistoryRepository;
import com.whylock.whylock.repository.UserRepository;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EnhancedReportService {

    private static final Logger log =
            LoggerFactory.getLogger(EnhancedReportService.class);

    private final JavaMailSender mailSender;
    private final UserRepository userRepository;
    private final ScanHistoryRepository scanHistoryRepository;
    private final PdfReportService pdfReportService;
    private final ObjectMapper objectMapper;

    public EnhancedReportService(
            JavaMailSender mailSender,
            UserRepository userRepository,
            ScanHistoryRepository scanHistoryRepository,
            PdfReportService pdfReportService,
            ObjectMapper objectMapper) {

        this.mailSender = mailSender;
        this.userRepository = userRepository;
        this.scanHistoryRepository = scanHistoryRepository;
        this.pdfReportService = pdfReportService;
        this.objectMapper = objectMapper;
    }
    @Transactional(readOnly = true)
    public void sendReportForResponse(User user, AiScanResponse response) {
        try {
            byte[] pdf = pdfReportService.generateReport(response);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(user.getEmail());
            helper.setSubject("🛡 WHYLOCK Security Report");
            helper.setText("""
                Hello %s,

                Your website security scan has completed successfully.

                Your AI-generated WHYLOCK Security Report is attached.

                Thank you for choosing WHYLOCK.

                Know Why A System Locks Before It Locks

                Regards,
                WHYLOCK Team
                """.formatted(user.getUsername()));

            helper.addAttachment("WHYLOCK-Security-Report.pdf", new ByteArrayResource(pdf));

            mailSender.send(message);
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
                        // FIX: removed duplicate readValue() call (was parsing twice)
                        response = objectMapper.readValue(full, AiScanResponse.class);

                        System.out.println("========== WHYLOCK PDF ==========");
                        System.out.println("Has 'findings' key in stored JSON: " + full.contains("\"findings\""));
                        System.out.println("Has 'vulnerabilities' key in stored JSON: " + full.contains("\"vulnerabilities\""));
                        System.out.println("Risk Score : " + response.getRiskScore());
                        System.out.println("Overall Risk : " + response.getOverallRisk());
                        System.out.println("Total Vulnerabilities : " + response.getTotalVulnerabilities());
                        System.out.println("Critical : " + response.getCriticalCount());
                        System.out.println("High : " + response.getHighCount());
                        System.out.println("Medium : " + response.getMediumCount());
                        System.out.println("Low : " + response.getLowCount());
                        System.out.println("Findings : " +
                                (response.getFindings() == null ? 0 : response.getFindings().size()));
                        System.out.println("Vulnerabilities : " +
                                (response.getVulnerabilities() == null ? 0 : response.getVulnerabilities().size()));
                        System.out.println("=================================");
                    } catch (Exception ex) {

                        log.warn(
                                "Unable to deserialize scan response for {}",
                                user.getUsername(),
                                ex
                        );

                    }

                }

            }

            if (response == null) {

                response = new AiScanResponse();

                response.setUrl("Account report for " + user.getUsername());

                response.setReportId(
                        "account-" +
                                user.getId() +
                                "-" +
                                System.currentTimeMillis()
                );

                response.setOverallRisk("UNKNOWN");
                response.setRiskScore(0);

                response.setSummary(
                        "No recent scans available for this account."
                );

                response.setScannedAt(
                        scans != null && !scans.isEmpty()
                                ? scans.get(0).getScannedAt()
                                : LocalDateTime.now()
                );

                response.setAiModel("WHYLOCK");

            }

            return pdfReportService.generateReport(response);

        } catch (Exception e) {

            log.error(
                    "Failed to generate PDF for {}",
                    user.getUsername(),
                    e
            );

            throw e;
        }
    }

    @Transactional
    public void sendReportToUser(User user) {

        try {

            byte[] pdf = generatePdfForUser(user);

            MimeMessage message = mailSender.createMimeMessage();

            MimeMessageHelper helper =
                    new MimeMessageHelper(message, true, "UTF-8");

            // Send to registered email
            helper.setTo(user.getEmail());

            helper.setSubject("🛡 WHYLOCK Security Report");

            helper.setText("""
                    Hello %s,

                    Your website security scan has completed successfully.

                    Your AI-generated WHYLOCK Security Report is attached.

                    Thank you for choosing WHYLOCK.

                    Know Why A System Locks Before It Locks

                    Regards,
                    WHYLOCK Team
                    """.formatted(user.getUsername()));

            helper.addAttachment(
                    "WHYLOCK-Security-Report.pdf",
                    new ByteArrayResource(pdf)
            );

            mailSender.send(message);

            log.info(
                    "WHYLOCK report sent successfully to {}",
                    user.getEmail()
            );

        } catch (Exception e) {

            log.error(
                    "Failed to send WHYLOCK report to {}",
                    user.getEmail(),
                    e
            );

        }

    }

    @Transactional(readOnly = true)
    public ResponseEntity<ByteArrayResource> generateAndDownloadForUser(
            User user) {

        try {

            byte[] pdf = generatePdfForUser(user);

            ByteArrayResource resource =
                    new ByteArrayResource(pdf);

            HttpHeaders headers = new HttpHeaders();

            headers.setContentType(MediaType.APPLICATION_PDF);

            headers.setContentDispositionFormData(
                    "inline",
                    "WHYLOCK-Security-Report.pdf"
            );

            return new ResponseEntity<>(
                    resource,
                    headers,
                    HttpStatus.OK
            );

        } catch (Exception e) {

            log.error(
                    "Failed to generate download for {}",
                    user.getUsername(),
                    e
            );

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();

        }
    }
}