package com.whylock.whylock.model;

public class FixResponse {
    private String cveId;
    private String fixCode;
    private String explanation;
    private String implementation;

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
}
