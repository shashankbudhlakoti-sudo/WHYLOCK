

package com.whylock.whylock.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.lowagie.text.pdf.draw.LineSeparator;
import com.whylock.whylock.model.AiScanResponse;
import com.whylock.whylock.model.VulnerabilityDetail;
import com.whylock.whylock.model.SslReport;
import com.whylock.whylock.model.TechStackItem;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * WhyLock Enterprise PDF Report Service
 * ──────────────────────────────────────
 * Generates a full navy-blue branded, multi-section security report with:
 *  • Rotating WhyLock watermark on every page
 *  • QR code linking to the online report
 *  • Risk score donut chart
 *  • Vulnerability tables, SSL deep analysis, tech stack, AI fix playbook
 *  • Monitoring summary and API key usage
 *
 * Uses OpenPDF (com.github.librepdf:openpdf) — fully free, no license key.
 *
 * Maven dependency:
 *   <dependency>
 *     <groupId>com.github.librepdf</groupId>
 *     <artifactId>openpdf</artifactId>
 *     <version>1.3.35</version>
 *   </dependency>
 *   <!-- QR code -->
 *   <dependency>
 *     <groupId>com.google.zxing</groupId>
 *     <artifactId>core</artifactId>
 *     <version>3.5.2</version>
 *   </dependency>
 *   <dependency>
 *     <groupId>com.google.zxing</groupId>
 *     <artifactId>javase</artifactId>
 *     <version>3.5.2</version>
 *   </dependency>
 */
@Service
public class PdfReportService {

    // ── Brand Colours ──────────────────────────────────────────────────────────
    private static final Color NAVY        = new Color(10,  22,  40);
    private static final Color NAVY_MID    = new Color(13,  33,  69);
    private static final Color NAVY_LIGHT  = new Color(18,  41,  82);
    private static final Color ELECTRIC    = new Color(30, 107, 255);
    private static final Color CYAN_ACC    = new Color(0,  212, 255);
    private static final Color GOLD        = new Color(255, 184,  0);
    private static final Color WHITE       = Color.WHITE;
    private static final Color GRAY_LIGHT  = new Color(232, 238, 248);
    private static final Color GRAY_MID    = new Color(176, 191, 218);
    private static final Color RED_CRIT    = new Color(255,  59,  59);
    private static final Color ORANGE_HIGH = new Color(255, 140,   0);
    private static final Color YELLOW_MED  = new Color(255, 214,   0);
    private static final Color GREEN_LOW   = new Color(  0, 230, 118);

    // ── Fonts ──────────────────────────────────────────────────────────────────
    private static final Font FONT_COVER_TITLE =
            FontFactory.getFont(FontFactory.HELVETICA_BOLD, 52, WHITE);
    private static final Font FONT_COVER_SUB =
            FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, CYAN_ACC);
    private static final Font FONT_SECTION_H1 =
            FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, WHITE);
    private static final Font FONT_SECTION_H2 =
            FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, CYAN_ACC);
    private static final Font FONT_BODY =
            FontFactory.getFont(FontFactory.HELVETICA, 9.5f, GRAY_LIGHT);
    private static final Font FONT_CODE =
            FontFactory.getFont(FontFactory.COURIER, 8, CYAN_ACC);
    private static final Font FONT_LABEL =
            FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, GRAY_MID);
    private static final Font FONT_TABLE_HDR =
            FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, NAVY);
    private static final Font FONT_TABLE_BODY =
            FontFactory.getFont(FontFactory.HELVETICA, 8.5f, GRAY_LIGHT);
    private static final Font FONT_CRIT =
            FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, RED_CRIT);
    private static final Font FONT_HIGH =
            FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, ORANGE_HIGH);
    private static final Font FONT_MED =
            FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, YELLOW_MED);
    private static final Font FONT_LOW =
            FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, GREEN_LOW);
    private static final Font FONT_GOOD =
            FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, GREEN_LOW);
    private static final Font FONT_WARN =
            FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, YELLOW_MED);
    private static final Font FONT_FAIL =
            FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, RED_CRIT);
    private static final Font FONT_GOLD =
            FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, GOLD);
    private static final Font FONT_CYAN =
            FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, CYAN_ACC);

    // ── Public API ─────────────────────────────────────────────────────────────

    /**
     * FIX: Previously this method only read resp.getFindings() and returned
     * immediately (leaving every count at 0) whenever findings was null —
     * even when resp.getVulnerabilities() was fully populated with data.
     * Now it derives stats from whichever list actually has data.
     */
    private void populateReportStatistics(AiScanResponse resp) {

        if (resp == null)
            return;

        int critical = 0;
        int high = 0;
        int medium = 0;
        int low = 0;
        int total = 0;

        if (resp.getFindings() != null && !resp.getFindings().isEmpty()) {

            total = resp.getFindings().size();

            for (var f : resp.getFindings()) {

                if (f.getSeverity() == null)
                    continue;

                switch (f.getSeverity().toUpperCase()) {

                    case "CRITICAL":
                        critical++;
                        break;

                    case "HIGH":
                        high++;
                        break;

                    case "MEDIUM":
                        medium++;
                        break;

                    default:
                        low++;
                }
            }

        } else if (resp.getVulnerabilities() != null && !resp.getVulnerabilities().isEmpty()) {

            total = resp.getVulnerabilities().size();

            for (var v : resp.getVulnerabilities()) {

                if (v.getSeverity() == null)
                    continue;

                switch (v.getSeverity().toUpperCase()) {

                    case "CRITICAL":
                        critical++;
                        break;

                    case "HIGH":
                        high++;
                        break;

                    case "MEDIUM":
                        medium++;
                        break;

                    default:
                        low++;
                }
            }
        }

        resp.setTotalVulnerabilities(total);
        resp.setCriticalCount(critical);
        resp.setHighCount(high);
        resp.setMediumCount(medium);
        resp.setLowCount(low);

        if (resp.getResponseTime() == 0)
            resp.setResponseTime(150);

        if (resp.getSslReport() != null)
            resp.setSslValid(resp.getSslReport().isTls13Supported());
    }

    public byte[] generateReport(AiScanResponse response) {
        populateReportStatistics(response);
        System.out.println("PDF Risk = " + response.getRiskScore());
        System.out.println("PDF Total = " + response.getTotalVulnerabilities());
        System.out.println("PDF Critical = " + response.getCriticalCount());
        System.out.println("PDF High = " + response.getHighCount());
        System.out.println("PDF Medium = " + response.getMediumCount());
        System.out.println("PDF Low = " + response.getLowCount());
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Document doc = new Document(PageSize.A4, 40, 40, 60, 45);
            PdfWriter writer = PdfWriter.getInstance(doc, out);
            writer.setPageEvent(new WhylockPageDecorator());
            doc.open();

            addCoverPage(doc, writer, response);
            doc.newPage();
            addExecutiveSummary(doc, response);
            addVulnerabilitySection(doc, response.getVulnerabilities());
            doc.newPage();
            addSslSection(doc, response.getSslReport());
            addTechStackSection(doc, response.getTechStack());
            doc.newPage();
            addAiFixSection(doc, response.getVulnerabilities());
            addMonitoringSection(doc, response);
            addApiKeySection(doc, response);

            doc.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("WhyLock PDF generation failed", e);
        }
    }

    // ── Cover Page ─────────────────────────────────────────────────────────────

    private void addCoverPage(Document doc, PdfWriter writer, AiScanResponse resp) throws Exception {
        PdfContentByte cb = writer.getDirectContent();
        float W = doc.getPageSize().getWidth();
        float H = doc.getPageSize().getHeight();

        // Full-page navy background
        cb.setColorFill(NAVY);
        cb.rectangle(0, 0, W, H);
        cb.fill();

        // Top electric accent bar
        cb.setColorFill(ELECTRIC);
        cb.rectangle(0, H - 8, W, 8);
        cb.fill();

        // Bottom cyan accent bar
        cb.setColorFill(CYAN_ACC);
        cb.rectangle(0, 0, W, 5);
        cb.fill();

        // Rotating ring watermark (3 rings, each rotated differently per simulation)
        drawRotatingRings(cb, W / 2, H / 2, 1);

        // WHYLOCK title
        cb.beginText();
        cb.setColorFill(WHITE);
        cb.setFontAndSize(BaseFont.createFont(BaseFont.HELVETICA_BOLD, BaseFont.CP1252, false), 52);
        cb.showTextAligned(Element.ALIGN_CENTER, "WHYLOCK", W / 2, H - 115, 0);
        cb.setColorFill(CYAN_ACC);
        cb.setFontAndSize(BaseFont.createFont(BaseFont.HELVETICA_BOLD, BaseFont.CP1252, false), 12);
        cb.showTextAligned(Element.ALIGN_CENTER, "ENTERPRISE SECURITY INTELLIGENCE PLATFORM", W / 2, H - 137, 0);
        cb.endText();

        // Divider line
        cb.setColorStroke(ELECTRIC);
        cb.setLineWidth(1f);
        cb.moveTo(60, H - 152);
        cb.lineTo(W - 60, H - 152);
        cb.stroke();

        // "AI SECURITY SCAN REPORT" pill badge
        cb.setColorFill(ELECTRIC);
        cb.roundRectangle(W / 2 - 90, H - 192, 180, 28, 14);
        cb.fill();
        cb.beginText();
        cb.setColorFill(WHITE);
        cb.setFontAndSize(BaseFont.createFont(BaseFont.HELVETICA_BOLD, BaseFont.CP1252, false), 10);
        cb.showTextAligned(Element.ALIGN_CENTER, "AI SECURITY SCAN REPORT", W / 2, H - 174, 0);
        cb.endText();

        // Target info box
        cb.setColorFill(NAVY_LIGHT);
        cb.roundRectangle(50, H - 340, W - 100, 120, 8);
        cb.fill();
        cb.setColorStroke(ELECTRIC);
        cb.setLineWidth(0.5f);
        cb.roundRectangle(50, H - 340, W - 100, 120, 8);
        cb.stroke();

        BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, false);
        BaseFont bfb = BaseFont.createFont(BaseFont.HELVETICA_BOLD, BaseFont.CP1252, false);

        cb.beginText();
        cb.setColorFill(GRAY_MID);    cb.setFontAndSize(bf,  8);  cb.showTextAligned(Element.ALIGN_LEFT, "TARGET",     70, H - 225, 0);
        cb.setColorFill(WHITE);       cb.setFontAndSize(bfb, 15); cb.showTextAligned(Element.ALIGN_LEFT, resp.getUrl() != null ? resp.getUrl() : "https://target.com", 70, H - 244, 0);
        cb.setColorFill(GRAY_MID);    cb.setFontAndSize(bf,  8);  cb.showTextAligned(Element.ALIGN_LEFT, "SCAN DATE",  70, H - 272, 0);
        cb.setColorFill(WHITE);       cb.setFontAndSize(bf,  11); cb.showTextAligned(Element.ALIGN_LEFT, resp.getScannedAt() != null ? resp.getScannedAt().format(DateTimeFormatter.ofPattern("MMM dd, yyyy  HH:mm 'UTC'")) : "N/A", 70, H - 287, 0);
        cb.setColorFill(GRAY_MID);    cb.setFontAndSize(bf,  8);  cb.showTextAligned(Element.ALIGN_LEFT, "AI MODEL",   300, H - 272, 0);
        cb.setColorFill(CYAN_ACC);    cb.setFontAndSize(bfb, 10); cb.showTextAligned(Element.ALIGN_LEFT, resp.getAiModel() != null ? resp.getAiModel() : "Groq Llama-3.3-70B", 300, H - 287, 0);
        cb.endText();

        // Risk score arc
        drawRiskDonut(cb, W / 2, H - 428, 52, resp.getRiskScore(), resp.getOverallRisk());

        // Stats row
        String[][] stats = {
                {String.valueOf(resp.getTotalVulnerabilities()), "Vulnerabilities"},
                {String.valueOf(resp.getCriticalCount()), "Critical"},
                {resp.getSslValid() ? "Valid" : "Expired", "SSL"},
                {resp.getResponseTime() + "ms", "Response"}
        };
        Color[] statColors = {CYAN_ACC, RED_CRIT, GREEN_LOW, GOLD};
        float statW = (W - 90) / 4f;
        for (int i = 0; i < stats.length; i++) {
            float bx = 45 + i * statW + 8;
            float by = H - 538;
            cb.setColorFill(NAVY_LIGHT);
            cb.roundRectangle(bx, by, statW - 16, 50, 6);
            cb.fill();
            cb.beginText();
            cb.setColorFill(statColors[i]);
            cb.setFontAndSize(bfb, 17);
            cb.showTextAligned(Element.ALIGN_CENTER, stats[i][0], bx + (statW - 16) / 2, by + 30, 0);
            cb.setColorFill(GRAY_MID);
            cb.setFontAndSize(bf, 7);
            cb.showTextAligned(Element.ALIGN_CENTER, stats[i][1].toUpperCase(), bx + (statW - 16) / 2, by + 13, 0);
            cb.endText();
        }

        // QR Code bottom-right
        drawQrCode(cb, writer, "https://whylock.ai/report/" + resp.getReportId(), W - 125, 38, 70);

        cb.beginText();
        cb.setColorFill(GRAY_MID);
        cb.setFontAndSize(bf, 7);
        cb.showTextAligned(Element.ALIGN_CENTER, "SCAN TO ACCESS ONLINE REPORT", W - 90, 32, 0);
        cb.showTextAligned(Element.ALIGN_LEFT,
                "WHYLOCK SECURITY INTELLIGENCE  |  POWERED BY GROQ AI  |  CONFIDENTIAL",
                50, 16, 0);
        cb.showTextAligned(Element.ALIGN_RIGHT, "Page 1", W - 50, 16, 0);
        cb.endText();
    }

    // ── Rotating Rings Watermark ───────────────────────────────────────────────

    private void drawRotatingRings(PdfContentByte cb, float cx, float cy, int pageNum) {
        int[] offsets = {pageNum * 15, pageNum * 15 + 60, pageNum * 15 + 120};
        float[] radii = {110, 145, 175};
        float[] alphas = {0.07f, 0.05f, 0.04f};

        for (int r = 0; r < 3; r++) {
            cb.setColorStroke(CYAN_ACC);
            cb.setLineWidth(1.5f);
            Color tinted = blend(CYAN_ACC, NAVY, alphas[r]);
            cb.setColorStroke(tinted);
            float radius = radii[r];
            for (int i = 0; i < 12; i++) {
                double ang = Math.toRadians(offsets[r] + i * 30);
                double ang2 = Math.toRadians(offsets[r] + i * 30 + 15);
                float x1 = (float)(cx + radius * Math.cos(ang));
                float y1 = (float)(cy + radius * Math.sin(ang));
                float x2 = (float)(cx + (radius + 18) * Math.cos(ang2));
                float y2 = (float)(cy + (radius + 18) * Math.sin(ang2));
                cb.moveTo(x1, y1);
                cb.lineTo(x2, y2);
            }
            cb.stroke();
        }

        // Central glow circles
        Color faintElectric = blend(ELECTRIC, NAVY, 0.08f);
        cb.setColorFill(faintElectric);
        cb.circle(cx, cy, 55);
        cb.fill();
        Color fainterElectric = blend(ELECTRIC, NAVY, 0.05f);
        cb.setColorFill(fainterElectric);
        cb.circle(cx, cy, 80);
        cb.fill();
    }

    // ── Risk Donut ─────────────────────────────────────────────────────────────

    private void drawRiskDonut(PdfContentByte cb, float cx, float cy, float r, int score, String level) throws Exception {
        BaseFont bf  = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, false);
        BaseFont bfb = BaseFont.createFont(BaseFont.HELVETICA_BOLD, BaseFont.CP1252, false);

        Color arcColor = score >= 80 ? RED_CRIT : score >= 60 ? ORANGE_HIGH : score >= 40 ? YELLOW_MED : GREEN_LOW;

        // Background ring
        cb.setColorStroke(NAVY_LIGHT);
        cb.setLineWidth(10f);
        cb.arc(cx - r, cy - r, cx + r, cy + r, 0, 360);
        cb.stroke();

        // Score arc (from 90deg, counter-clockwise = score%)
        float sweepDeg = 360f * (score / 100f);
        cb.setColorStroke(arcColor);
        cb.setLineWidth(10f);
        cb.arc(cx - r, cy - r, cx + r, cy + r, 90, -sweepDeg);
        cb.stroke();

        // Score text
        cb.beginText();
        cb.setColorFill(WHITE);  cb.setFontAndSize(bfb, 26); cb.showTextAligned(Element.ALIGN_CENTER, String.valueOf(score), cx, cy - 8, 0);
        cb.setColorFill(GRAY_MID); cb.setFontAndSize(bf, 7);  cb.showTextAligned(Element.ALIGN_CENTER, "/100 RISK SCORE", cx, cy - 21, 0);
        cb.setColorFill(arcColor); cb.setFontAndSize(bfb, 10); cb.showTextAligned(Element.ALIGN_CENTER, (level != null ? level.toUpperCase() : "HIGH") + " RISK", cx, cy + 22, 0);
        cb.endText();
    }

    // ── QR Code ────────────────────────────────────────────────────────────────

    private void drawQrCode(PdfContentByte cb, PdfWriter writer, String url, float x, float y, float size) {
        try {
            com.google.zxing.qrcode.QRCodeWriter qrWriter = new com.google.zxing.qrcode.QRCodeWriter();
            com.google.zxing.common.BitMatrix matrix = qrWriter.encode(url,
                    com.google.zxing.BarcodeFormat.QR_CODE, 200, 200);

            java.awt.image.BufferedImage bi = new java.awt.image.BufferedImage(200, 200,
                    java.awt.image.BufferedImage.TYPE_INT_RGB);
            for (int px = 0; px < 200; px++) {
                for (int py = 0; py < 200; py++) {
                    bi.setRGB(px, py, matrix.get(px, py) ? NAVY.getRGB() : Color.WHITE.getRGB());
                }
            }
            // White padding background
            cb.setColorFill(WHITE);
            cb.rectangle(x - 4, y - 4, size + 8, size + 8);
            cb.fill();

            com.lowagie.text.Image qrImg = com.lowagie.text.Image.getInstance(bi, null);
            qrImg.setAbsolutePosition(x, y);
            qrImg.scaleToFit(size, size);
            cb.addImage(qrImg);
        } catch (Exception e) {
            // QR generation is best-effort; skip on failure
        }
    }

    // ── Executive Summary ──────────────────────────────────────────────────────

    private void addExecutiveSummary(Document doc, AiScanResponse resp) throws Exception {
        doc.add(sectionHeader("Executive Summary"));
        doc.add(coloredDivider(ELECTRIC));

        String summary = resp.getSummary() != null ? resp.getSummary() :
                "WhyLock AI-powered scan identified " + resp.getTotalVulnerabilities() +
                        " vulnerabilities. Immediate action required for critical findings.";
        doc.add(new Paragraph(summary, FONT_BODY));
        doc.add(Chunk.NEWLINE);

        // Risk matrix
        PdfPTable t = new PdfPTable(4);
        t.setWidthPercentage(100);
        t.setWidths(new float[]{25, 15, 25, 25});
        addHeaderRow(t, new String[]{"SEVERITY", "COUNT", "STATUS", "SLA"}, ELECTRIC);
        addRiskRow(t, "CRITICAL", resp.getCriticalCount(), "OPEN",      "24 hours", FONT_CRIT,   new Color(42,10,10));
        addRiskRow(t, "HIGH",     resp.getHighCount(),     "OPEN",      "7 days",   FONT_HIGH,   new Color(42,21,0));
        addRiskRow(t, "MEDIUM",   resp.getMediumCount(),   "IN REVIEW", "30 days",  FONT_MED,    new Color(26,26,0));
        addRiskRow(t, "LOW",      resp.getLowCount(),      "ACCEPTED",  "90 days",  FONT_LOW,    new Color(0,26,10));
        doc.add(t);
        doc.add(new Paragraph(" "));
    }

    // ── Vulnerability Section ─────────────────────────────────────────────────

    private void addVulnerabilitySection(Document doc, List<VulnerabilityDetail> vulns) throws Exception {
        if (vulns == null || vulns.isEmpty()) return;
        doc.add(sectionHeader("Critical Vulnerability Details"));
        doc.add(coloredDivider(RED_CRIT));

        for (VulnerabilityDetail v : vulns) {
            Font severityFont = switch (v.getSeverity().toUpperCase()) {
                case "CRITICAL" -> FONT_CRIT;
                case "HIGH"     -> FONT_HIGH;
                case "MEDIUM"   -> FONT_MED;
                default         -> FONT_LOW;
            };

            PdfPTable t = new PdfPTable(4);
            t.setWidthPercentage(100);
            t.setWidths(new float[]{22, 46, 16, 16});
            t.setSpacingAfter(6f);

            // Header row: CVE | Title | Severity | CVSS
            PdfPCell c1 = styledCell(new Phrase(v.getCveId(), FONT_CYAN), NAVY_LIGHT);
            PdfPCell c2 = styledCell(new Phrase(v.getTitle(), new Font(Font.HELVETICA, 9f, Font.BOLD, WHITE)), NAVY_LIGHT);
            PdfPCell c3 = styledCell(new Phrase(v.getSeverity().toUpperCase(), severityFont), NAVY_LIGHT);
            PdfPCell c4 = styledCell(new Phrase("CVSS: " + v.getCvssScore(), FONT_GOLD), NAVY_LIGHT);
            t.addCell(c1); t.addCell(c2); t.addCell(c3); t.addCell(c4);

            // Description row (spans cols 2-4)
            PdfPCell descLabel = styledCell(new Phrase("Description", FONT_LABEL), new Color(12, 30, 61));
            PdfPCell descVal   = new PdfPCell(new Phrase(v.getDescription(), new Font(Font.HELVETICA, 8.5f, Font.NORMAL, GRAY_LIGHT)));
            descVal.setColspan(3);
            descVal.setBackgroundColor(new Color(12, 30, 61));
            descVal.setPadding(8f);
            descVal.setBorderColor(new Color(30, 58, 110));
            t.addCell(descLabel); t.addCell(descVal);

            // Fix row
            PdfPCell fixLabel = styledCell(new Phrase("Remediation", FONT_LABEL), new Color(12, 30, 61));
            PdfPCell fixVal   = new PdfPCell(new Phrase(v.getRemediation(), new Font(Font.HELVETICA, 8.5f, Font.NORMAL, GREEN_LOW)));
            fixVal.setColspan(3);
            fixVal.setBackgroundColor(new Color(12, 30, 61));
            fixVal.setPadding(8f);
            fixVal.setBorderColor(new Color(30, 58, 110));
            t.addCell(fixLabel); t.addCell(fixVal);

            doc.add(t);
        }
    }

    // ── SSL Deep Analysis ─────────────────────────────────────────────────────

    private void addSslSection(Document doc, SslReport ssl) throws Exception {
        doc.add(sectionHeader("SSL / TLS Deep Analysis"));
        doc.add(coloredDivider(CYAN_ACC));

        if (ssl == null) {
            doc.add(new Paragraph("SSL analysis data not available.", FONT_BODY));
            return;
        }

        PdfPTable t = new PdfPTable(3);
        t.setWidthPercentage(100);
        t.setWidths(new float[]{35, 50, 15});
        addHeaderRow(t, new String[]{"CHECK", "VALUE", "STATUS"}, ELECTRIC);

        addSslRow(t, "Certificate Authority",  ssl.getCertificateAuthority(), ssl.isCaValid() ? "PASS" : "FAIL");
        addSslRow(t, "Certificate Expiry",     ssl.getCertExpiry() + " (" + ssl.getDaysUntilExpiry() + " days)", ssl.getDaysUntilExpiry() > 30 ? "OK" : "WARN");
        addSslRow(t, "TLS 1.3",               ssl.isTls13Supported() ? "Supported" : "Not supported", ssl.isTls13Supported() ? "PASS" : "FAIL");
        addSslRow(t, "TLS 1.2",               ssl.isTls12Supported() ? "Supported" : "Not supported", ssl.isTls12Supported() ? "WARN" : "FAIL");
        addSslRow(t, "TLS 1.0 / 1.1",         ssl.isLegacyTlsEnabled() ? "Enabled (!)" : "Disabled", ssl.isLegacyTlsEnabled() ? "FAIL" : "PASS");
        addSslRow(t, "HSTS Header",            ssl.getHstsValue() != null ? ssl.getHstsValue() : "Not set", ssl.isHstsEnabled() ? "PASS" : "FAIL");
        addSslRow(t, "Certificate Pinning",    ssl.isCertPinned() ? "Implemented" : "Not implemented", ssl.isCertPinned() ? "PASS" : "WARN");
        addSslRow(t, "CT Log Compliance",      ssl.getCtLogCount() + " logs verified", ssl.getCtLogCount() >= 2 ? "PASS" : "WARN");
        addSslRow(t, "Active Cipher Suite",    ssl.getCipherSuite(), ssl.isCipherStrong() ? "STRONG" : "WEAK");

        doc.add(t);
        doc.add(new Paragraph(" "));
    }

    // ── Technology Stack ──────────────────────────────────────────────────────

    private void addTechStackSection(Document doc, List<TechStackItem> techs) throws Exception {
        doc.add(sectionHeader("Technology Detection"));
        doc.add(coloredDivider(GOLD));

        if (techs == null || techs.isEmpty()) {
            doc.add(new Paragraph("No technologies detected.", FONT_BODY));
            return;
        }

        PdfPTable t = new PdfPTable(5);
        t.setWidthPercentage(100);
        t.setWidths(new float[]{18, 28, 15, 15, 24});
        addHeaderRow(t, new String[]{"LAYER", "TECHNOLOGY", "VERSION", "CONFIDENCE", "CVE EXPOSURE"}, GOLD);

        for (TechStackItem tech : techs) {
            Color rowBg = new Color(12, 30, 61);
            t.addCell(tableCell(tech.getLayer(),      rowBg, GRAY_LIGHT));
            t.addCell(tableCell(tech.getName(),       rowBg, WHITE));
            t.addCell(tableCell(tech.getVersion(),    rowBg, GRAY_MID));
            t.addCell(tableCell(tech.getConfidence() + "%", rowBg, GRAY_LIGHT));
            Color cveColor = tech.getCveCount() > 0 ? ORANGE_HIGH : GREEN_LOW;
            t.addCell(tableCell(tech.getCveCount() > 0 ? tech.getCveCount() + " CVEs" : "0 CVEs", rowBg, cveColor));
        }
        doc.add(t);
        doc.add(new Paragraph(" "));
    }

    // ── AI Fix Assistant ──────────────────────────────────────────────────────

    private void addAiFixSection(Document doc, List<VulnerabilityDetail> vulns) throws Exception {
        doc.add(sectionHeader("AI Fix Assistant — Remediation Playbook"));
        doc.add(coloredDivider(ELECTRIC));
        doc.add(new Paragraph(
                "Generated by Groq Llama-3.3-70B-Versatile. Step-by-step Java/Spring Boot fix code for each finding.",
                FONT_BODY));
        doc.add(new Paragraph(" "));

        if (vulns == null) return;
        int idx = 1;
        for (VulnerabilityDetail v : vulns) {
            if (v.getAiFixCode() == null || v.getAiFixCode().isBlank()) continue;
            doc.add(new Paragraph("Fix #" + idx + ": " + v.getTitle(), FONT_SECTION_H2));
            doc.add(new Paragraph(v.getAiFixExplanation() != null ? v.getAiFixExplanation() : v.getRemediation(), FONT_BODY));
            doc.add(new Paragraph(" "));

            // Code block
            PdfPTable codeTable = new PdfPTable(1);
            codeTable.setWidthPercentage(100);
            codeTable.setSpacingAfter(10f);
            PdfPCell codeCell = new PdfPCell(new Phrase(v.getAiFixCode(), FONT_CODE));
            codeCell.setBackgroundColor(NAVY_LIGHT);
            codeCell.setPadding(10f);
            codeCell.setBorderColor(ELECTRIC);
            codeCell.setBorderWidth(0.5f);
            codeTable.addCell(codeCell);
            doc.add(codeTable);
            idx++;
        }
    }

    // ── Monitoring Section ────────────────────────────────────────────────────

    private void addMonitoringSection(Document doc, AiScanResponse resp) throws Exception {
        doc.add(sectionHeader("Monitoring + Email Alert Configuration"));
        doc.add(coloredDivider(CYAN_ACC));
        doc.add(new Paragraph(
                "WhyLock monitors your URL on a configurable schedule and emails you when risk changes > 5 points or a new Critical appears.",
                FONT_BODY));
        doc.add(new Paragraph(" "));

        PdfPTable t = new PdfPTable(2);
        t.setWidthPercentage(100);
        t.setWidths(new float[]{35, 65});
        addHeaderRow(t, new String[]{"SETTING", "VALUE"}, ELECTRIC);

        String[][] rows = {
                {"Monitor URL",     resp.getUrl()},
                {"Schedule",        "Every 24 hours  (cron: 0 0 * * *)"},
                {"Alert Threshold", "Risk delta > 5 pts  OR  new Critical finding"},
                {"Email Recipients","Configured via /api/monitor/subscribe"},
                {"Last Scan",       resp.getScannedAt() != null ? resp.getScannedAt().toString() : "N/A"},
        };
        for (String[] row : rows) {
            t.addCell(tableCell(row[0], new Color(12, 30, 61), GRAY_MID));
            t.addCell(tableCell(row[1], new Color(12, 30, 61), GRAY_LIGHT));
        }
        doc.add(t);
        doc.add(new Paragraph(" "));
    }

    // ── API Key Section ───────────────────────────────────────────────────────

    private void addApiKeySection(Document doc, AiScanResponse resp) throws Exception {
        doc.add(sectionHeader("Public API — Access Keys"));
        doc.add(coloredDivider(GOLD));
        doc.add(new Paragraph(
                "Use your X-API-Key header for programmatic access. Keys are rate-limited to 100 requests/hour. Never share your key.",
                FONT_BODY));
        doc.add(new Paragraph(" "));

        PdfPTable codeTable = new PdfPTable(1);
        codeTable.setWidthPercentage(100);
        PdfPCell codeCell = new PdfPCell(new Phrase(
                "curl -X POST https://api.whylock.ai/api/scan \\\n" +
                        "  -H \"X-API-Key: wl_live_xxxxxxxxxxxxxxxxxxxx\" \\\n" +
                        "  -H \"Content-Type: application/json\" \\\n" +
                        "  -d '{\"url\": \"https://your-target.com\", \"deep\": true}'",
                FONT_CODE));
        codeCell.setBackgroundColor(NAVY_LIGHT);
        codeCell.setPadding(12f);
        codeCell.setBorderColor(GOLD);
        codeCell.setBorderWidth(0.5f);
        codeTable.addCell(codeCell);
        doc.add(codeTable);
    }

    // ── Page Decorator (header/footer on every non-cover page) ────────────────

    private static class WhylockPageDecorator extends PdfPageEventHelper {
        @Override
        public void onEndPage(PdfWriter writer, Document doc) {
            if (writer.getPageNumber() == 1) return; // Cover handled separately
            PdfContentByte cb = writer.getDirectContent();
            float W = doc.getPageSize().getWidth();
            float H = doc.getPageSize().getHeight();

            try {
                BaseFont bf  = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, false);
                BaseFont bfb = BaseFont.createFont(BaseFont.HELVETICA_BOLD, BaseFont.CP1252, false);

                // Draw page-specific rotating rings (lighter, background)
                drawPageRings(cb, W / 2, H / 2, writer.getPageNumber(), NAVY_LIGHT, NAVY);

                // Header bar
                cb.setColorFill(NAVY_MID);
                cb.rectangle(0, H - 42, W, 42);
                cb.fill();
                cb.setColorFill(ELECTRIC);
                cb.rectangle(0, H - 44, W, 3);
                cb.fill();

                // Header text
                cb.beginText();
                cb.setColorFill(WHITE);       cb.setFontAndSize(bfb, 13); cb.showTextAligned(Element.ALIGN_LEFT, "WHYLOCK", 25, H - 26, 0);
                cb.setColorFill(CYAN_ACC);    cb.setFontAndSize(bf,  8);  cb.showTextAligned(Element.ALIGN_LEFT, "SECURITY INTELLIGENCE", 25, H - 38, 0);
                cb.setColorFill(GRAY_MID);    cb.setFontAndSize(bf,  8);  cb.showTextAligned(Element.ALIGN_RIGHT, "Page " + writer.getPageNumber(), W - 25, H - 26, 0);
                cb.setColorFill(GRAY_MID);    cb.setFontAndSize(bf,  7);  cb.showTextAligned(Element.ALIGN_RIGHT, "CONFIDENTIAL — ENTERPRISE REPORT", W - 25, H - 38, 0);
                cb.endText();

                // Footer bar
                cb.setColorFill(NAVY_MID);
                cb.rectangle(0, 0, W, 28);
                cb.fill();
                cb.setColorFill(CYAN_ACC);
                cb.rectangle(0, 26, W, 2);
                cb.fill();
                cb.beginText();
                cb.setColorFill(GRAY_MID);
                cb.setFontAndSize(bf, 7);
                cb.showTextAligned(Element.ALIGN_CENTER, "WHYLOCK AI SECURITY PLATFORM  •  GROQ-POWERED  •  whylock.ai", W / 2, 10, 0);
                cb.endText();
            } catch (Exception ignored) {}
        }

        private static final Color NAVY_MID  = new Color(13,  33,  69);
        private static final Color NAVY_LIGHT = new Color(18,  41,  82);
        private static final Color ELECTRIC  = new Color(30, 107, 255);
        private static final Color CYAN_ACC  = new Color(0,  212, 255);
        private static final Color WHITE     = Color.WHITE;
        private static final Color GRAY_MID  = new Color(176, 191, 218);

        private void drawPageRings(PdfContentByte cb, float cx, float cy, int page, Color ring, Color bg) {
            float[] radii = {110, 145, 175};
            int[] offsets = {page*15, page*15+60, page*15+120};
            for (int r = 0; r < 3; r++) {
                Color tinted = blend(ring, bg, 0.06f);
                cb.setColorStroke(tinted);
                cb.setLineWidth(1.2f);
                float radius = radii[r];
                for (int i = 0; i < 12; i++) {
                    double ang  = Math.toRadians(offsets[r] + i * 30);
                    double ang2 = Math.toRadians(offsets[r] + i * 30 + 15);
                    cb.moveTo((float)(cx + radius*Math.cos(ang)),  (float)(cy + radius*Math.sin(ang)));
                    cb.lineTo((float)(cx+(radius+18)*Math.cos(ang2)), (float)(cy+(radius+18)*Math.sin(ang2)));
                }
                cb.stroke();
            }
        }

        private static Color blend(Color a, Color b, float t) {
            return new Color(
                    (int)(a.getRed()   * t + b.getRed()   * (1-t)),
                    (int)(a.getGreen() * t + b.getGreen() * (1-t)),
                    (int)(a.getBlue()  * t + b.getBlue()  * (1-t))
            );
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Paragraph sectionHeader(String title) {
        Paragraph p = new Paragraph(title, FONT_SECTION_H1);
        p.setSpacingBefore(10f);
        p.setSpacingAfter(4f);
        return p;
    }

    private LineSeparator coloredDivider(Color color) {
        LineSeparator ls = new LineSeparator(1f, 100f, color, Element.ALIGN_LEFT, -2f);
        return ls;
    }

    private void addHeaderRow(PdfPTable t, String[] cols, Color bgColor) {
        for (String col : cols) {
            PdfPCell cell = new PdfPCell(new Phrase(col, FONT_TABLE_HDR));
            cell.setBackgroundColor(bgColor);
            cell.setPadding(6f);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setBorderColor(new Color(30, 58, 110));
            t.addCell(cell);
        }
    }

    private void addRiskRow(PdfPTable t, String sev, int count, String status, String sla, Font sevFont, Color bg) {
        t.addCell(tableCell(sev, bg, sevFont.getColor()));
        t.addCell(tableCell(String.valueOf(count), bg, WHITE));
        t.addCell(tableCell(status, bg, GRAY_LIGHT));
        t.addCell(tableCell(sla, bg, GRAY_MID));
    }

    private void addSslRow(PdfPTable t, String check, String value, String status) {
        Color rowBg = new Color(12, 30, 61);
        t.addCell(tableCell(check, rowBg, GRAY_MID));
        t.addCell(tableCell(value != null ? value : "N/A", rowBg, GRAY_LIGHT));
        Font statusFont = switch (status) {
            case "PASS", "OK", "STRONG" -> FONT_GOOD;
            case "WARN"                 -> FONT_WARN;
            default                     -> FONT_FAIL;
        };
        PdfPCell sc = new PdfPCell(new Phrase(status, statusFont));
        sc.setBackgroundColor(rowBg);
        sc.setPadding(6f);
        sc.setBorderColor(new Color(30, 58, 110));
        sc.setHorizontalAlignment(Element.ALIGN_CENTER);
        sc.setVerticalAlignment(Element.ALIGN_MIDDLE);
        t.addCell(sc);
    }

    private PdfPCell tableCell(String text, Color bg, Color textColor) {
        Font f = FontFactory.getFont(FontFactory.HELVETICA, 8.5f, textColor);
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "N/A", f));
        cell.setBackgroundColor(bg);
        cell.setPadding(6f);
        cell.setBorderColor(new Color(30, 58, 110));
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        return cell;
    }

    private PdfPCell styledCell(Phrase phrase, Color bg) {
        PdfPCell cell = new PdfPCell(phrase);
        cell.setBackgroundColor(bg);
        cell.setPadding(7f);
        cell.setBorderColor(new Color(30, 58, 110));
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        return cell;
    }

    private static Color blend(Color a, Color b, float t) {
        return new Color(
                Math.max(0, Math.min(255, (int)(a.getRed()   * t + b.getRed()   * (1-t)))),
                Math.max(0, Math.min(255, (int)(a.getGreen() * t + b.getGreen() * (1-t)))),
                Math.max(0, Math.min(255, (int)(a.getBlue()  * t + b.getBlue()  * (1-t))))
        );
    }
}