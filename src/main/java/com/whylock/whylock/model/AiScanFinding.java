package com.whylock.whylock.model;

public class AiScanFinding {

    private String title;
    private String severity;
    private String description;
    private String fixCode;
    private String cveMatch;

    public AiScanFinding() {}

    public AiScanFinding(String title, String severity, String description,
                         String fixCode, String cveMatch) {
        this.title = title;
        this.severity = severity;
        this.description = description;
        this.fixCode = fixCode;
        this.cveMatch = cveMatch;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getFixCode() { return fixCode; }
    public void setFixCode(String fixCode) { this.fixCode = fixCode; }

    public String getCveMatch() { return cveMatch; }
    public void setCveMatch(String cveMatch) { this.cveMatch = cveMatch; }
}