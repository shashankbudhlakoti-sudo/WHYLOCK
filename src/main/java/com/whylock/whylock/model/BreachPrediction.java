package com.whylock.whylock.model;

import java.util.List;
import java.util.Map;

public class BreachPrediction {

    private String url;
    private String breachProbability;
    private String estimatedTimeToBreach;
    private String summary;
    private List<Map<String, String>> attackVectors;
    private List<String> priorityFixes;

    public BreachPrediction() {}

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getBreachProbability() { return breachProbability; }
    public void setBreachProbability(String breachProbability) { this.breachProbability = breachProbability; }

    public String getEstimatedTimeToBreach() { return estimatedTimeToBreach; }
    public void setEstimatedTimeToBreach(String estimatedTimeToBreach) { this.estimatedTimeToBreach = estimatedTimeToBreach; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public List<Map<String, String>> getAttackVectors() { return attackVectors; }
    public void setAttackVectors(List<Map<String, String>> attackVectors) { this.attackVectors = attackVectors; }

    public List<String> getPriorityFixes() { return priorityFixes; }
    public void setPriorityFixes(List<String> priorityFixes) { this.priorityFixes = priorityFixes; }
}