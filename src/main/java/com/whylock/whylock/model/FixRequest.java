package com.whylock.whylock.model;

public class FixRequest {
    private String cveId;
    private String vulnerability;
    private String codeSnippet;

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
}
