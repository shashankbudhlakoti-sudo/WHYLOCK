package com.whylock.whylock.model;

public class TechStackItem {
    private String layer;
    private String name;
    private String version;
    private int confidence;
    private int cveCount;
    private boolean hasCveExposure;

    public TechStackItem() {}

    public TechStackItem(String layer, String name, String version, int confidence, int cveCount) {
        this.layer = layer;
        this.name = name;
        this.version = version;
        this.confidence = confidence;
        this.cveCount = cveCount;
    }

    public String getLayer() { return layer; }
    public void setLayer(String layer) { this.layer = layer; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public int getConfidence() { return confidence; }
    public void setConfidence(int confidence) { this.confidence = confidence; }

    public int getCveCount() { return cveCount; }
    public void setCveCount(int cveCount) { this.cveCount = cveCount; }

    public boolean isHasCveExposure() { return hasCveExposure; }
    public void setHasCveExposure(boolean hasCveExposure) { this.hasCveExposure = hasCveExposure; }
}
