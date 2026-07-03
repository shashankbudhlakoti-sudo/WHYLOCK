package com.whylock.whylock.scanner;

import org.springframework.stereotype.Service;

import javax.net.ssl.HttpsURLConnection;
import java.net.HttpURLConnection;
import java.net.URI;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Service
public class UrlScannerService {

    public Map<String, Object> scan(String targetUrl) {

        Map<String, Object> result = new LinkedHashMap<>();
        List<String> risks = new ArrayList<>();
        int riskScore = 0;

        try {
            URI uri = URI.create(targetUrl);
            HttpURLConnection connection =
                    (HttpURLConnection) uri.toURL().openConnection();

            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setInstanceFollowRedirects(true);

            int responseCode = connection.getResponseCode();

            // ── Basic info ────────────────────────────────────────
            result.put("url", targetUrl);
            result.put("responseCode", responseCode);

            if (responseCode == 200) {
                result.put("status", "reachable");
            } else {
                result.put("status", "unexpected response: " + responseCode);
                riskScore += 10;
            }

            // ── SSL check ─────────────────────────────────────────
            if (targetUrl.startsWith("https://")) {
                result.put("ssl", "present");

                try {
                    HttpsURLConnection httpsConn =
                            (HttpsURLConnection) uri.toURL().openConnection();
                    httpsConn.setConnectTimeout(5000);
                    httpsConn.connect();

                    X509Certificate cert =
                            (X509Certificate) httpsConn.getServerCertificates()[0];

                    long daysLeft = Duration.between(
                            Instant.now(),
                            cert.getNotAfter().toInstant()
                    ).toDays();

                    result.put("sslExpiresInDays", daysLeft);

                    if (daysLeft <= 0) {
                        risks.add("SSL certificate is expired");
                        riskScore += 40;
                    } else if (daysLeft <= 30) {
                        risks.add("SSL certificate expiring in " + daysLeft + " days");
                        riskScore += 15;
                    } else {
                        result.put("sslStatus", "valid");
                    }

                    httpsConn.disconnect();

                } catch (Exception e) {
                    risks.add("SSL certificate invalid or untrusted");
                    riskScore += 40;
                }

            } else {
                result.put("ssl", "none — site uses plain HTTP");
                risks.add("No SSL — all traffic is unencrypted");
                riskScore += 30;
            }

            // ── Security headers check ────────────────────────────
            Map<String, String> headersFound = new LinkedHashMap<>();
            List<String> missingHeaders = new ArrayList<>();

            checkHeader(connection, "Strict-Transport-Security", headersFound, missingHeaders);
            checkHeader(connection, "Content-Security-Policy", headersFound, missingHeaders);
            checkHeader(connection, "X-Frame-Options", headersFound, missingHeaders);
            checkHeader(connection, "X-Content-Type-Options", headersFound, missingHeaders);
            checkHeader(connection, "Referrer-Policy", headersFound, missingHeaders);

            result.put("securityHeadersPresent", headersFound);

            if (!missingHeaders.isEmpty()) {
                risks.add("Missing security headers: " + missingHeaders);
                riskScore += missingHeaders.size() * 5;
            }

            // ── Open redirect check ───────────────────────────────
            String lower = targetUrl.toLowerCase();
            List<String> redirectPatterns =
                    List.of("url=http", "redirect=http", "next=http", "return=http");

            for (String pattern : redirectPatterns) {
                if (lower.contains(pattern)) {
                    risks.add("Possible open redirect detected in URL");
                    riskScore += 25;
                    break;
                }
            }

            connection.disconnect();

        } catch (Exception e) {
            result.put("status", "scan failed");
            result.put("error", e.getMessage());
            risks.add("Could not reach target URL");
            riskScore += 20;
        }

        // ── Final risk score ──────────────────────────────────────
        riskScore = Math.min(riskScore, 100);
        result.put("riskScore", riskScore);
        result.put("riskLevel", getRiskLevel(riskScore));
        result.put("risks", risks);
        result.put("safe", risks.isEmpty());

        return result;
    }

    private void checkHeader(HttpURLConnection conn, String headerName,
                             Map<String, String> found, List<String> missing) {
        String value = conn.getHeaderField(headerName);
        if (value != null) {
            found.put(headerName, value);
        } else {
            missing.add(headerName);
        }
    }

    private String getRiskLevel(int score) {
        if (score >= 70) return "HIGH";
        if (score >= 40) return "MEDIUM";
        if (score >= 20) return "LOW";
        return "SAFE";
    }
}