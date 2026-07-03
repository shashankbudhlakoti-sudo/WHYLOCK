package com.whylock.whylock.dto;

import java.util.List;

public class FixResponse {
    private String cveId;
    private String fixCode;
    private String explanation;
    private String implementation;
    private String codeSnippet;
    private List<String> steps;
    private List<String> references;
    private String title;
    private String estimatedFixTime;
    private String riskIfIgnored;

    public FixResponse() {}

    public FixResponse(String cveId, String fixCode, String explanation, String implementation) {
        this.cveId = cveId;
        this.fixCode = fixCode;
        this.explanation = explanation;
        this.implementation = implementation;
    }

    public String getCveId() { return cveId; }
    public void setCveId(String cveId) { this.cveId = cveId; }

    public String getFixCode() { return fixCode; }
    public void setFixCode(String fixCode) { this.fixCode = fixCode; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }

    public String getImplementation() { return implementation; }
    public void setImplementation(String implementation) { this.implementation = implementation; }

    public String getCodeSnippet() { return codeSnippet; }
    public void setCodeSnippet(String codeSnippet) { this.codeSnippet = codeSnippet; }

    public List<String> getSteps() { return steps; }
    public void setSteps(List<String> steps) { this.steps = steps; }

    public List<String> getReferences() { return references; }
    public void setReferences(List<String> references) { this.references = references; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getEstimatedFixTime() { return estimatedFixTime; }
    public void setEstimatedFixTime(String estimatedFixTime) { this.estimatedFixTime = estimatedFixTime; }

    public String getRiskIfIgnored() { return riskIfIgnored; }
    public void setRiskIfIgnored(String riskIfIgnored) { this.riskIfIgnored = riskIfIgnored; }
}
