package model;

import util.MyArrayList;

public class RiskAssessment {
    private double finalScore;
    private String riskTier;
    private MyArrayList<String> riskFactorsList;
    private boolean isBlocked;

    public RiskAssessment() {}

    public RiskAssessment(double finalScore, String riskTier, MyArrayList<String> riskFactorsList, boolean isBlocked) {
        this.finalScore = finalScore;
        this.riskTier = riskTier;
        this.riskFactorsList = riskFactorsList;
        this.isBlocked = isBlocked;
    }

    public double getFinalScore() {
        return finalScore;
    }

    public void setFinalScore(double finalScore) {
        this.finalScore = finalScore;
    }

    public String getRiskTier() {
        return riskTier;
    }

    public void setRiskTier(String riskTier) {
        this.riskTier = riskTier;
    }

    public MyArrayList<String> getRiskFactorsList() {
        return riskFactorsList;
    }

    public void setRiskFactorsList(MyArrayList<String> riskFactorsList) {
        this.riskFactorsList = riskFactorsList;
    }

    public boolean isBlocked() {
        return isBlocked;
    }

    public void setBlocked(boolean blocked) {
        isBlocked = blocked;
    }

    @Override
    public String toString() {
        return "RiskAssessment{" +
                "finalScore=" + finalScore +
                ", riskTier='" + riskTier + '\'' +
                ", isBlocked=" + isBlocked +
                '}';
    }
}
