package com.whylock.whylock.scanner;

import com.whylock.whylock.model.SubdomainResult;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.Map;
@Service
public class SubdomainScannerService {

    // Common subdomains to check
    private static final List<String> COMMON_SUBDOMAINS = List.of(
            "www", "mail", "ftp", "api", "dev", "staging", "test",
            "admin", "blog", "shop", "store", "app", "mobile",
            "portal", "secure", "vpn", "cdn", "static", "assets",
            "images", "img", "media", "docs", "help", "support",
            "login", "auth", "oauth", "status", "monitor", "dashboard",
            "db", "database", "backup", "beta", "demo", "sandbox",
            "git", "gitlab", "jenkins", "ci", "prod", "production"
    );

    private final UrlScannerService urlScannerService;
    private final HttpClient httpClient;

    public SubdomainScannerService(UrlScannerService urlScannerService) {
        this.urlScannerService = urlScannerService;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build();
    }

    /**
     * Discovers and scans all subdomains for a given domain.
     * Runs checks in parallel for speed.
     */
    public List<SubdomainResult> scanSubdomains(String domain) {
        // Clean domain — remove http/https/www
        String cleanDomain = extractDomain(domain);

        List<SubdomainResult> results = new ArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(10);
        List<Future<SubdomainResult>> futures = new ArrayList<>();

        // Check each subdomain in parallel
        for (String sub : COMMON_SUBDOMAINS) {
            String fullDomain = sub + "." + cleanDomain;
            futures.add(executor.submit(() -> checkSubdomain(fullDomain)));
        }

        // Collect results
        for (Future<SubdomainResult> future : futures) {
            try {
                SubdomainResult result = future.get(5, TimeUnit.SECONDS);
                if (result != null) {
                    results.add(result);
                }
            } catch (Exception e) {
                // timeout or error — skip this subdomain
            }
        }

        executor.shutdown();

        // Sort — alive first, then by risk score descending
        results.sort((a, b) -> {
            if (a.isAlive() && !b.isAlive()) return -1;
            if (!a.isAlive() && b.isAlive()) return 1;
            return Integer.compare(b.getRiskScore(), a.getRiskScore());
        });

        return results;
    }

    // ─── Check single subdomain ───────────────────────────────────────────────

    private SubdomainResult checkSubdomain(String fullDomain) {
        SubdomainResult result = new SubdomainResult();
        result.setSubdomain(fullDomain);

        // Step 1 — DNS resolution check
        try {
            InetAddress address = InetAddress.getByName(fullDomain);
            result.setIpAddress(address.getHostAddress());
            result.setAlive(true);
        } catch (Exception e) {
            result.setAlive(false);
            result.setStatus("DNS not found");
            return result; // not alive — return early
        }

        // Step 2 — HTTP check (try HTTPS first, then HTTP)
        String url = "https://" + fullDomain;
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(3))
                    .GET()
                    .build();
            HttpResponse<Void> response = httpClient.send(
                    request, HttpResponse.BodyHandlers.discarding());
            result.setHttpStatus(response.statusCode());
            result.setStatus("reachable");
            result.setSsl("present");
        } catch (Exception e) {
            // Try plain HTTP
            try {
                url = "http://" + fullDomain;
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .timeout(Duration.ofSeconds(3))
                        .GET()
                        .build();
                HttpResponse<Void> response = httpClient.send(
                        request, HttpResponse.BodyHandlers.discarding());
                result.setHttpStatus(response.statusCode());
                result.setStatus("reachable — HTTP only");
                result.setSsl("none");
                result.setRiskScore(result.getRiskScore() + 30);
            } catch (Exception ex) {
                result.setStatus("unreachable");
                result.setSsl("unknown");
            }
        }

        // Step 3 — Quick security scan
        try {
            Map<String, Object> scan = urlScannerService.scan(url);
            Object score = scan.get("riskScore");
            Object level = scan.get("riskLevel");
            if (score instanceof Integer) result.setRiskScore((Integer) score);
            if (level instanceof String) result.setRiskLevel((String) level);
        } catch (Exception e) {
            result.setRiskLevel("UNKNOWN");
        }

        return result;
    }

    // ─── Domain extractor ─────────────────────────────────────────────────────

    private String extractDomain(String input) {
        try {
            if (!input.startsWith("http")) input = "https://" + input;
            URI uri = URI.create(input);
            String host = uri.getHost();
            // Remove www. prefix if present
            if (host != null && host.startsWith("www.")) {
                host = host.substring(4);
            }
            return host != null ? host : input;
        } catch (Exception e) {
            return input.replace("https://", "")
                    .replace("http://", "")
                    .replace("www.", "");
        }
    }
}