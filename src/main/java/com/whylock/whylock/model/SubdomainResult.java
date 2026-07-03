package com.whylock.whylock.model;

public class SubdomainResult {

    private String subdomain;
    private String ipAddress;
    private boolean alive;
    private String status;
    private String ssl;
    private int httpStatus;
    private int riskScore;
    private String riskLevel;

    public SubdomainResult() {
        this.riskScore = 0;
        this.riskLevel = "UNKNOWN";
        this.alive = false;
    }

    public String getSubdomain() { return subdomain; }
    public void setSubdomain(String subdomain) { this.subdomain = subdomain; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public boolean isAlive() { return alive; }
    public void setAlive(boolean alive) { this.alive = alive; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getSsl() { return ssl; }
    public void setSsl(String ssl) { this.ssl = ssl; }

    public int getHttpStatus() { return httpStatus; }
    public void setHttpStatus(int httpStatus) { this.httpStatus = httpStatus; }

    public int getRiskScore() { return riskScore; }
    public void setRiskScore(int riskScore) { this.riskScore = riskScore; }

    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
}