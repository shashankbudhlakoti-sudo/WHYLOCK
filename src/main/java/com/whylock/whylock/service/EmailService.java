package com.whylock.whylock.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendWelcomeEmail(String to, String username) {

        try {

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("🛡 Welcome to WHYLOCK");

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

                        <h2 style="color:#2563eb;">
                            Welcome %s 👋
                        </h2>

                        <p style="font-size:16px;color:#444;line-height:1.8;">

                            Your WHYLOCK account has been created successfully.

                        </p>

                        <p style="font-size:16px;color:#444;line-height:1.8;">

                            You can now scan websites, generate AI-powered
                            security reports, download PDF reports,
                            and receive reports directly in your email.

                        </p>

                        <div style="
                        margin:30px 0;
                        padding:20px;
                        border-radius:12px;
                        background:#eff6ff;
                        border-left:5px solid #2563eb;">

                            <b>Security Engine Ready</b><br>
                            AI Analysis Enabled<br>
                            PDF Reports Enabled<br>
                            Email Reports Enabled

                        </div>

                        <p style="margin-top:35px;">
                        Thank you for choosing WHYLOCK.
                        </p>

                    </div>

                    <div style="
                    padding:25px;
                    background:#0f172a;
                    color:white;
                    text-align:center;">

                        WHYLOCK © 2026

                    </div>

                </div>

                </body>
                </html>
                """.formatted(username);

            helper.setText(html, true);

            mailSender.send(message);

        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }

    }

}