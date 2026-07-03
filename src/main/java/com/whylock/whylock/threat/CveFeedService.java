package com.whylock.whylock.threat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;

import java.time.LocalDate;
import java.util.*;

/**
 * WHYLOCK CVE Feed Service
 *
 * Fetches real vulnerability data from NIST NVD API v2.
 * Free public API — no key required.
 * Falls back to mock data if API is unreachable.
 *
 * Presentation point:
 * WHYLOCK is globally context-aware — every login and scan
 * is checked against real-world active threats automatically.
 */
@Service
public class CveFeedService {

    private static final Logger log = LoggerFactory.getLogger(CveFeedService.class);

    private static final String NVD_CRITICAL =
            "https://services.nvd.nist.gov/rest/json/cves/2.0?cvssV3Severity=CRITICAL&resultsPerPage=10";

    private static final String NVD_HIGH =
            "https://services.nvd.nist.gov/rest/json/cves/2.0?cvssV3Severity=HIGH&resultsPerPage=10";

    private final RestTemplate restTemplate = new RestTemplate();

    // ── Public API ────────────────────────────────────────────────

    public Map<String, Object> getStatus() {
        List<CveEntry> cves = fetchCves();
        boolean isLive = cves.stream().anyMatch(c -> "NIST_NVD".equals(c.getSource()));

        long criticalCount = cves.stream()
                .filter(c -> "CRITICAL".equals(c.getSeverity())).count();
        long exploitedCount = cves.stream()
                .filter(CveEntry::isActivelyExploited).count();

        Map<String, Object> status = new LinkedHashMap<>();
        status.put("dataSource", isLive ? "NIST_NVD" : "MOCK_FALLBACK");
        status.put("isLive", isLive);
        status.put("totalCves", cves.size());
        status.put("criticalCount", criticalCount);
        status.put("activelyExploitedCount", exploitedCount);
        status.put("note", isLive
                ? "Connected to NIST NVD — data is real-time"
                : "NIST API unreachable — using WHYLOCK mock dataset");
        return status;
    }

    public List<CveEntry> getCriticalCves() {
        return fetchCves().stream()
                .filter(c -> "CRITICAL".equals(c.getSeverity()))
                .toList();
    }

    public List<CveEntry> getActivelyExploited() {
        return fetchCves().stream()
                .filter(CveEntry::isActivelyExploited)
                .toList();
    }

    public List<CveEntry> getAllCves() {
        return fetchCves();
    }

    // ── Fetch logic — real API then mock fallback ─────────────────

    public List<CveEntry> fetchCves() {
        try {
            log.info("WHYLOCK: Fetching CVE data from NIST NVD...");
            List<CveEntry> cves = new ArrayList<>();
            cves.addAll(parseNvdResponse(restTemplate.getForObject(NVD_CRITICAL, Map.class)));
            cves.addAll(parseNvdResponse(restTemplate.getForObject(NVD_HIGH, Map.class)));
            if (!cves.isEmpty()) {
                log.info("WHYLOCK: Fetched {} CVEs from NIST NVD", cves.size());
                return cves;
            }
        } catch (RestClientException e) {
            log.warn("WHYLOCK: NIST NVD unreachable — using mock fallback: {}", e.getMessage());
        } catch (Exception e) {
            log.warn("WHYLOCK: CVE fetch error — using mock fallback: {}", e.getMessage());
        }

        log.info("WHYLOCK: Loaded {} mock CVEs", getMockCves().size());
        return getMockCves();
    }

    // ── NIST NVD JSON parser ──────────────────────────────────────

    @SuppressWarnings("unchecked")
    private List<CveEntry> parseNvdResponse(Map<String, Object> response) {
        List<CveEntry> results = new ArrayList<>();
        if (response == null) return results;

        List<Map<String, Object>> vulnerabilities =
                (List<Map<String, Object>>) response.get("vulnerabilities");
        if (vulnerabilities == null) return results;

        for (Map<String, Object> vuln : vulnerabilities) {
            try {
                Map<String, Object> cve = (Map<String, Object>) vuln.get("cve");
                if (cve == null) continue;

                String cveId = (String) cve.get("id");

                // description
                String description = "No description";
                List<Map<String, Object>> descs =
                        (List<Map<String, Object>>) cve.get("descriptions");
                if (descs != null) {
                    for (Map<String, Object> d : descs) {
                        if ("en".equals(d.get("lang"))) {
                            description = (String) d.get("value");
                            break;
                        }
                    }
                }

                // CVSS score
                double cvssScore = 0.0;
                Map<String, Object> metrics = (Map<String, Object>) cve.get("metrics");
                if (metrics != null) {
                    List<Map<String, Object>> cvssV3 =
                            (List<Map<String, Object>>) metrics.get("cvssMetricV31");
                    if (cvssV3 != null && !cvssV3.isEmpty()) {
                        Map<String, Object> data =
                                (Map<String, Object>) cvssV3.get(0).get("cvssData");
                        if (data != null && data.get("baseScore") instanceof Number n) {
                            cvssScore = n.doubleValue();
                        }
                    }
                }

                // severity
                String severity = cvssScore >= 9.0 ? "CRITICAL"
                        : cvssScore >= 7.0 ? "HIGH"
                        : cvssScore >= 4.0 ? "MEDIUM" : "LOW";

                // published date
                String pubStr = (String) cve.get("published");
                LocalDate published = pubStr != null
                        ? LocalDate.parse(pubStr.substring(0, 10))
                        : LocalDate.now();

                results.add(new CveEntry(
                        cveId, description, cvssScore, severity,
                        published, cvssScore >= 9.0, "Various", "NIST_NVD"
                ));

            } catch (Exception e) {
                log.debug("Skipping CVE entry: {}", e.getMessage());
            }
        }
        return results;
    }

    // ── Mock fallback — real CVE IDs from 2023–2024 ──────────────

    private List<CveEntry> getMockCves() {
        return List.of(
                new CveEntry("CVE-2024-3400",
                        "PAN-OS command injection in GlobalProtect — actively exploited",
                        10.0, "CRITICAL", LocalDate.of(2024, 4, 12),
                        true, "Palo Alto Networks PAN-OS", "MOCK_FALLBACK"),

                new CveEntry("CVE-2024-21762",
                        "Fortinet FortiOS out-of-bounds write — remote code execution without auth",
                        9.8, "CRITICAL", LocalDate.of(2024, 2, 8),
                        true, "Fortinet FortiOS", "MOCK_FALLBACK"),

                new CveEntry("CVE-2023-46805",
                        "Ivanti Connect Secure authentication bypass — mass exploitation observed",
                        8.2, "HIGH", LocalDate.of(2024, 1, 10),
                        true, "Ivanti Connect Secure", "MOCK_FALLBACK"),

                new CveEntry("CVE-2024-1709",
                        "ConnectWise ScreenConnect auth bypass — CISA emergency directive",
                        10.0, "CRITICAL", LocalDate.of(2024, 2, 19),
                        true, "ConnectWise ScreenConnect", "MOCK_FALLBACK"),

                new CveEntry("CVE-2023-44487",
                        "HTTP/2 Rapid Reset Attack — record-breaking DDoS vector",
                        7.5, "HIGH", LocalDate.of(2023, 10, 10),
                        true, "HTTP/2 Protocol", "MOCK_FALLBACK"),

                new CveEntry("CVE-2021-44228",
                        "Log4Shell — remote code execution via JNDI lookup in Log4j",
                        10.0, "CRITICAL", LocalDate.of(2021, 12, 10),
                        true, "Apache Log4j", "MOCK_FALLBACK"),

                new CveEntry("CVE-2024-6387",
                        "OpenSSH regreSSHion — race condition allowing RCE as root",
                        8.1, "HIGH", LocalDate.of(2024, 7, 1),
                        true, "OpenSSH", "MOCK_FALLBACK")
        );
    }
}