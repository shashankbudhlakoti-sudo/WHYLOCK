package com.whylock.whylock.service;

import com.whylock.whylock.model.AiScanFinding;
import com.whylock.whylock.model.AiScanResponse;
import com.whylock.whylock.model.User;
import com.whylock.whylock.model.VulnerabilityDetail;
import com.whylock.whylock.repository.UserRepository;
import com.whylock.whylock.scanner.OsintMetaService;
import com.whylock.whylock.scanner.UrlScannerService;
import com.whylock.whylock.threat.CveEntry;
import com.whylock.whylock.threat.CveFeedService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AiScanOrchestrator {

    private final UrlScannerService urlScannerService;
    private final OsintMetaService osintMetaService;
    private final CveFeedService cveFeedService;
    private final AiService geminiAiService;
    private final ScanHistoryService scanHistoryService;
    private final EnhancedReportService enhancedReportService;
    private final UserRepository userRepository;

    public AiScanOrchestrator(
            UrlScannerService urlScannerService,
            OsintMetaService osintMetaService,
            CveFeedService cveFeedService,
            AiService geminiAiService,
            ScanHistoryService scanHistoryService,
            EnhancedReportService enhancedReportService,
            UserRepository userRepository) {

        this.urlScannerService = urlScannerService;
        this.osintMetaService = osintMetaService;
        this.cveFeedService = cveFeedService;
        this.geminiAiService = geminiAiService;
        this.scanHistoryService = scanHistoryService;
        this.enhancedReportService = enhancedReportService;
        this.userRepository = userRepository;
    }

    public AiScanResponse runFullAiScan(String url, String username) {

        Map<String, Object> scanData = runUrlScan(url);
        Map<String, String> osintData = osintMetaService.collect(url);
        List<Map<String, Object>> cveData = getCveContext();

        AiScanResponse response =
                geminiAiService.analyze(url, scanData, osintData, cveData);
        response.setResponseTime(
                ((Number) scanData.getOrDefault("responseTime", 0)).longValue()
        );
        response.setSslValid(
                Boolean.TRUE.equals(scanData.get("sslValid"))
        );

        // FIX: don't blindly wipe AI-provided data — only default when missing.
        if (response.getTechStack() == null) {
            response.setTechStack(new ArrayList<>());
        }

        if (response.getVulnerabilities() == null || response.getVulnerabilities().isEmpty()) {
            response.setVulnerabilities(mapFindingsToVulnerabilities(response.getFindings()));
        }

        // Step 5 — Save History
        scanHistoryService.save(username, response);

        // Step 6 — Send PDF Report Email (FIX: send the in-memory response directly,
        // instead of re-fetching + re-deserializing from the DB)
        try {
            User user = userRepository.findByUsername(username).orElse(null);
            if (user != null) {
                enhancedReportService.sendReportForResponse(user, response);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return response;
    }

    private List<VulnerabilityDetail> mapFindingsToVulnerabilities(List<AiScanFinding> findings) {
        if (findings == null || findings.isEmpty()) {
            return new ArrayList<>();
        }

        return findings.stream().map(f -> {
            VulnerabilityDetail v = new VulnerabilityDetail();
            v.setCveId(f.getCveMatch() != null && !f.getCveMatch().isBlank() ? f.getCveMatch() : "N/A");
            v.setTitle(f.getTitle());
            v.setSeverity(f.getSeverity());
            v.setDescription(f.getDescription());
            v.setRemediation(f.getFixCode() != null && !f.getFixCode().isBlank()
                    ? f.getFixCode() : "See AI Fix Assistant section for remediation code.");
            v.setAiFixCode(f.getFixCode());
            v.setAiFixExplanation(f.getDescription());
            v.setCvssScore(0.0);
            return v;
        }).collect(Collectors.toList());
    }

    private Map<String, Object> runUrlScan(String url) {
        try {
            return urlScannerService.scan(url);
        } catch (Exception e) {
            return Map.of("scanError", "URL scan failed: " + e.getMessage());
        }
    }

    private List<Map<String, Object>> getCveContext() {
        try {
            List<CveEntry> cves = cveFeedService.getAllCves();
            return cves.stream()
                    .limit(20)
                    .map(cve -> {
                        Map<String, Object> map = new java.util.LinkedHashMap<>();
                        map.put("id", cve.getCveId());
                        map.put("description", cve.getDescription());
                        map.put("cvssScore", cve.getCvssScore());
                        map.put("severity", cve.getSeverity());
                        map.put("affectedProduct", cve.getAffectedProduct());
                        map.put("exploited", cve.isActivelyExploited());
                        return map;
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            return List.of(Map.of("cveError", "CVE feed unavailable: " + e.getMessage()));
        }
    }
}