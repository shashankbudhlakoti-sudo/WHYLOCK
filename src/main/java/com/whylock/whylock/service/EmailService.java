package com.whylock.whylock.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
public class EmailService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${resend.api.key}")
    private String resendApiKey;

    @Value("${resend.from.email}")
    private String fromEmail;

    private static final String RESEND_URL = "https://api.resend.com/emails";

    public EmailService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // ===========================
    // Welcome Email
    // ===========================
    public void sendWelcomeEmail(String to, String username) {

        String html = """
            <html>
            <body style="margin:0;padding:40px;background:#f5f7fb;font-family:Arial,sans-serif;">
            <div style="max-width:650px;margin:auto;background:white;border-radius:18px;
            box-shadow:0 10px 30px rgba(0,0,0,.08);overflow:hidden;">

                <div style="background:#0f172a;padding:35px;text-align:center;">
                    <h1 style="color:white;margin:0;font-size:34px;">WHYLOCK</h1>
                    <p style="color:#94a3b8;margin-top:8px;">
                        Know Why A System Locks Before It Locks
                    </p>
                </div>

                <div style="padding:40px;">
                    <h2 style="color:#2563eb;">Welcome %s 👋</h2>

                    <p style="font-size:16px;color:#444;line-height:1.8;">
                        Your WHYLOCK account has been created successfully.
                    </p>

                    <p style="font-size:16px;color:#444;line-height:1.8;">
                        You can now scan websites, generate AI-powered
                        security reports, download PDF reports,
                        and receive reports directly in your email.
                    </p>

                    <div style="margin:30px 0;padding:20px;border-radius:12px;
                    background:#eff6ff;border-left:5px solid #2563eb;">

                        <b>Security Engine Ready</b><br>
                        AI Analysis Enabled<br>
                        PDF Reports Enabled<br>
                        Email Reports Enabled

                    </div>

                    <p style="margin-top:35px;">
                        Thank you for choosing WHYLOCK.
                    </p>

                </div>

                <div style="padding:25px;background:#0f172a;color:white;text-align:center;">
                    WHYLOCK © 2026
                </div>

            </div>
            </body>
            </html>
            """.formatted(username);

        sendHtmlEmail(to, "🛡 Welcome to WHYLOCK", html);
    }

    // ===========================
    // HTML Email
    // ===========================
    public void sendHtmlEmail(String to, String subject, String html) {
        send(to, subject, html);
    }

    // ===========================
    // HTML Email + Attachment
    // ===========================
    public void sendHtmlEmailWithAttachment(
            String to,
            String subject,
            String html,
            String attachmentFilename,
            byte[] attachmentBytes) {

        try {

            Map<String, Object> attachment = new HashMap<>();
            attachment.put("filename", attachmentFilename);
            attachment.put(
                    "content",
                    Base64.getEncoder().encodeToString(attachmentBytes)
            );

            Map<String, Object> body = new HashMap<>();
            body.put("from", fromEmail);
            body.put("to", new String[]{to});
            body.put("subject", subject);
            body.put("html", html);
            body.put("attachments", new Object[]{attachment});

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(resendApiKey);

            HttpEntity<String> request =
                    new HttpEntity<>(
                            objectMapper.writeValueAsString(body),
                            headers
                    );

            restTemplate.postForEntity(
                    RESEND_URL,
                    request,
                    String.class
            );

        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to send email with attachment via Resend",
                    e
            );
        }
    }

    // ===========================
    // Internal Send Method
    // ===========================
    private void send(String to, String subject, String html) {

        try {

            Map<String, Object> body = new HashMap<>();
            body.put("from", fromEmail);
            body.put("to", new String[]{to});
            body.put("subject", subject);
            body.put("html", html);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(resendApiKey);

            HttpEntity<String> request =
                    new HttpEntity<>(
                            objectMapper.writeValueAsString(body),
                            headers
                    );

            restTemplate.postForEntity(
                    RESEND_URL,
                    request,
                    String.class
            );

        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to send email via Resend",
                    e
            );
        }
    }

}
