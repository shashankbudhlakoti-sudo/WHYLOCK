package com.whylock.whylock.threat;

import java.time.LocalDate;

public class CveEntry {

    private String cveId;
    private String description;
    private double cvssScore;
    private String severity;
    private LocalDate publishedDate;
    private boolean activelyExploited;
    private String affectedProduct;
    private String source;

    public CveEntry() {}

    public CveEntry(String cveId, String description, double cvssScore,
                    String severity, LocalDate publishedDate,
                    boolean activelyExploited, String affectedProduct, String source) {
        this.cveId = cveId;
        this.description = description;
        this.cvssScore = cvssScore;
        this.severity = severity;
        this.publishedDate = publishedDate;
        this.activelyExploited = activelyExploited;
        this.affectedProduct = affectedProduct;
        this.source = source;
    }

    public String getCveId() { return cveId; }
    public String getDescription() { return description; }
    public double getCvssScore() { return cvssScore; }
    public String getSeverity() { return severity; }
    public LocalDate getPublishedDate() { return publishedDate; }
    public boolean isActivelyExploited() { return activelyExploited; }
    public String getAffectedProduct() { return affectedProduct; }
    public String getSource() { return source; }
}