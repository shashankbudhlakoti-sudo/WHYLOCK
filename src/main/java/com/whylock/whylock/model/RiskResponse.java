package com.whylock.whylock.model;

import java.util.List;

public class RiskResponse {

    private int trustScore;
    private String riskLevel;
    private String action;
    private double confidenceScore;
    private List<String> reasons;
    private double volatility;

    public RiskResponse(int trustScore, String riskLevel, String action,
                        double confidenceScore, List<String> reasons, double volatility) {
        this.trustScore = trustScore;
        this.riskLevel = riskLevel;
        this.action = action;
        this.confidenceScore = confidenceScore;
        this.reasons = reasons;
        this.volatility = volatility;
    }

    public int getTrustScore() { return trustScore; }
    public String getRiskLevel() { return riskLevel; }
    public String getAction() { return action; }
    public double getConfidenceScore() { return confidenceScore; }
    public List<String> getReasons() { return reasons; }
    public double getVolatility() { return volatility; }
}