package com.whylock.whylock.risk;


import java.util.List;
import java.util.Map;

public class RiskEvaluation {

    private int score;
    private RiskLevel level;

    private List<RiskFactor> triggeredFactors;

    private Map<RiskFactor,Integer> factorWeights;

    private List<String> explanations;

    private String subject;

    private String sourceIp;

}