package com.whylock.whylock.dto;

public class FixRequest {
    private String cveId;
    private String vulnerability;
    private String codeSnippet;
    private String vulnerabilityType;
    private String title;
    private String severity;
    private String techStack;
    private String context;

    public FixRequest() {}

    public FixRequest(String cveId, String vulnerability, String codeSnippet) {
        this.cveId = cveId;
        this.vulnerability = vulnerability;
        this.codeSnippet = codeSnippet;
    }

    public String getCveId() { return cveId; }
    public void setCveId(String cveId) { this.cveId = cveId; }

    public String getVulnerability() { return vulnerability; }
    public void setVulnerability(String vulnerability) { this.vulnerability = vulnerability; }

    public String getCodeSnippet() { return codeSnippet; }
    public void setCodeSnippet(String codeSnippet) { this.codeSnippet = codeSnippet; }

    public String getVulnerabilityType() { return vulnerabilityType; }
    public void setVulnerabilityType(String vulnerabilityType) { this.vulnerabilityType = vulnerabilityType; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public String getTechStack() { return techStack; }
    public void setTechStack(String techStack) { this.techStack = techStack; }

    public String getContext() { return context; }
    public void setContext(String context) { this.context = context; }
}
