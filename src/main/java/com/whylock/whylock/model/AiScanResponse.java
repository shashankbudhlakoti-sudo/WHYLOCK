package com.whylock.whylock.model;

import java.time.LocalDateTime;
import java.util.List;

public class AiScanResponse {

    private String url;
    private String reportId;
    private String overallRisk;          // CRITICAL / HIGH / MEDIUM / LOW / SAFE
    private int riskScore;               // 0–100
    private String summary;             // AI-generated plain English summary
    private List<AiScanFinding> findings; // all problems found
    private List<VulnerabilityDetail> vulnerabilities;
    private List<TechStackItem> techStack;
    private SslReport sslReport;
    private List<String> globalThreats;  // world-level CVE / threat intel
    private LocalDateTime scannedAt;           // timestamp
    private String aiModel;             // which model analysed this
    private int totalVulnerabilities;
    private int criticalCount;
    private int highCount;
    private int mediumCount;
    private int lowCount;
    private boolean sslValid;
    private long responseTime;

    public AiScanResponse() {}

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getReportId() { return reportId; }
    public void setReportId(String reportId) { this.reportId = reportId; }

    public String getOverallRisk() { return overallRisk; }
    public void setOverallRisk(String overallRisk) { this.overallRisk = overallRisk; }

    public int getRiskScore() { return riskScore; }
    public void setRiskScore(int riskScore) { this.riskScore = riskScore; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public List<AiScanFinding> getFindings() { return findings; }
    public void setFindings(List<AiScanFinding> findings) { this.findings = findings; }

    public List<VulnerabilityDetail> getVulnerabilities() { return vulnerabilities; }
    public void setVulnerabilities(List<VulnerabilityDetail> vulnerabilities) { this.vulnerabilities = vulnerabilities; }

    public List<TechStackItem> getTechStack() { return techStack; }
    public void setTechStack(List<TechStackItem> techStack) { this.techStack = techStack; }

    public SslReport getSslReport() { return sslReport; }
    public void setSslReport(SslReport sslReport) { this.sslReport = sslReport; }

    public List<String> getGlobalThreats() { return globalThreats; }
    public void setGlobalThreats(List<String> globalThreats) { this.globalThreats = globalThreats; }

    public LocalDateTime getScannedAt() { return scannedAt; }
    public void setScannedAt(LocalDateTime scannedAt) { this.scannedAt = scannedAt; }

    public String getAiModel() { return aiModel; }
    public void setAiModel(String aiModel) { this.aiModel = aiModel; }

    public int getTotalVulnerabilities() { return totalVulnerabilities; }
    public void setTotalVulnerabilities(int totalVulnerabilities) { this.totalVulnerabilities = totalVulnerabilities; }

    public int getCriticalCount() { return criticalCount; }
    public void setCriticalCount(int criticalCount) { this.criticalCount = criticalCount; }

    public int getHighCount() { return highCount; }
    public void setHighCount(int highCount) { this.highCount = highCount; }

    public int getMediumCount() { return mediumCount; }
    public void setMediumCount(int mediumCount) { this.mediumCount = mediumCount; }

    public int getLowCount() { return lowCount; }
    public void setLowCount(int lowCount) { this.lowCount = lowCount; }

    public boolean getSslValid() { return sslValid; }
    public void setSslValid(boolean sslValid) { this.sslValid = sslValid; }

    public long getResponseTime() { return responseTime; }
    public void setResponseTime(long responseTime) { this.responseTime = responseTime; }

    public long getScanDurationMs() { return responseTime; }
    public void setScanDurationMs(long scanDurationMs) { this.responseTime = scanDurationMs; }
}