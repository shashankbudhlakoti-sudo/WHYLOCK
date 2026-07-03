package com.whylock.whylock.dto;

public class ScanRequest {

    public enum ScanType { URL_SCAN, CLOUD_SCAN }

    private String targetUrl;
    private ScanType scanType;
    private String requestedByUser;
    private boolean deepScan;

    public String getTargetUrl() { return targetUrl; }
    public void setTargetUrl(String targetUrl) { this.targetUrl = targetUrl; }

    public ScanType getScanType() { return scanType; }
    public void setScanType(ScanType scanType) { this.scanType = scanType; }

    public String getRequestedByUser() { return requestedByUser; }
    public void setRequestedByUser(String requestedByUser) { this.requestedByUser = requestedByUser; }

    public boolean isDeepScan() { return deepScan; }
    public void setDeepScan(boolean deepScan) { this.deepScan = deepScan; }
}