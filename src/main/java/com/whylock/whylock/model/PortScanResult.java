package com.whylock.whylock.model;

public class PortScanResult {

    private int port;
    private String service;
    private boolean open;
    private String status;
    private String riskLevel;
    private String description;

    public PortScanResult() {}

    public int getPort() { return port; }
    public void setPort(int port) { this.port = port; }

    public String getService() { return service; }
    public void setService(String service) { this.service = service; }

    public boolean isOpen() { return open; }
    public void setOpen(boolean open) { this.open = open; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}