package com.fraud.detection.model;

public class AlgorithmBenchmarkResult {
    private String algorithmName;
    private int matchesFound;
    private long comparisonsCount;
    private long executionTimeNano;
    private String timeComplexity;
    private String spaceComplexity;

    public AlgorithmBenchmarkResult() {}

    public AlgorithmBenchmarkResult(String algorithmName, int matchesFound, long comparisonsCount, 
                                    long executionTimeNano, String timeComplexity, String spaceComplexity) {
        this.algorithmName = algorithmName;
        this.matchesFound = matchesFound;
        this.comparisonsCount = comparisonsCount;
        this.executionTimeNano = executionTimeNano;
        this.timeComplexity = timeComplexity;
        this.spaceComplexity = spaceComplexity;
    }

    public String getAlgorithmName() {
        return algorithmName;
    }

    public void setAlgorithmName(String algorithmName) {
        this.algorithmName = algorithmName;
    }

    public int getMatchesFound() {
        return matchesFound;
    }

    public void setMatchesFound(int matchesFound) {
        this.matchesFound = matchesFound;
    }

    public long getComparisonsCount() {
        return comparisonsCount;
    }

    public void setComparisonsCount(long comparisonsCount) {
        this.comparisonsCount = comparisonsCount;
    }

    public long getExecutionTimeNano() {
        return executionTimeNano;
    }

    public void setExecutionTimeNano(long executionTimeNano) {
        this.executionTimeNano = executionTimeNano;
    }

    public String getTimeComplexity() {
        return timeComplexity;
    }

    public void setTimeComplexity(String timeComplexity) {
        this.timeComplexity = timeComplexity;
    }

    public String getSpaceComplexity() {
        return spaceComplexity;
    }

    public void setSpaceComplexity(String spaceComplexity) {
        this.spaceComplexity = spaceComplexity;
    }

    @Override
    public String toString() {
        return "AlgorithmBenchmarkResult{" +
                "algorithmName='" + algorithmName + '\'' +
                ", matchesFound=" + matchesFound +
                ", comparisonsCount=" + comparisonsCount +
                ", executionTimeNano=" + executionTimeNano +
                ", timeComplexity='" + timeComplexity + '\'' +
                ", spaceComplexity='" + spaceComplexity + '\'' +
                '}';
    }
}
