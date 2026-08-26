package com.whylock.whylock.model;

public class LoginRequest {

    private String username;

    private boolean suspiciousIp;
    private boolean knownDevice;
    private boolean unusualTime;

    // 🔥 NEW SIGNALS
    private int failedAttempts;
    private double amount;
    private boolean newDevice;

    // Getters & Setters

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean isSuspiciousIp() {
        return suspiciousIp;
    }

    public void setSuspiciousIp(boolean suspiciousIp) {
        this.suspiciousIp = suspiciousIp;
    }

    public boolean isKnownDevice() {
        return knownDevice;
    }

    public void setKnownDevice(boolean knownDevice) {
        this.knownDevice = knownDevice;
    }

    public boolean isUnusualTime() {
        return unusualTime;
    }

    public void setUnusualTime(boolean unusualTime) {
        this.unusualTime = unusualTime;
    }

    public int getFailedAttempts() {
        return failedAttempts;
    }

    public void setFailedAttempts(int failedAttempts) {
        this.failedAttempts = failedAttempts;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public boolean isNewDevice() {
        return newDevice;
    }

    public void setNewDevice(boolean newDevice) {
        this.newDevice = newDevice;
    }
}