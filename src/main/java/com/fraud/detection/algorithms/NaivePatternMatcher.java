package com.fraud.detection.algorithms;

import com.fraud.detection.model.AlgorithmBenchmarkResult;

public class NaivePatternMatcher implements PatternMatcher {

    @Override
    public AlgorithmBenchmarkResult search(String text, String pattern) {
        long startTime = System.nanoTime();
        int matchesFound = 0;
        long comparisonsCount = 0;

        if (text == null || pattern == null) {
            long duration = System.nanoTime() - startTime;
            return new AlgorithmBenchmarkResult("Naive (Brute Force)", 0, 0, duration, "O(N*M)", "O(1)");
        }

        int n = text.length();
        int m = pattern.length();

        if (m == 0 || n < m) {
            long duration = System.nanoTime() - startTime;
            return new AlgorithmBenchmarkResult("Naive (Brute Force)", 0, 0, duration, "O(N*M)", "O(1)");
        }

        for (int i = 0; i <= n - m; i++) {
            boolean match = true;
            for (int j = 0; j < m; j++) {
                comparisonsCount++;
                if (text.charAt(i + j) != pattern.charAt(j)) {
                    match = false;
                    break;
                }
            }
            if (match) {
                matchesFound++;
            }
        }

        long endTime = System.nanoTime();
        long executionTimeNano = endTime - startTime;

        return new AlgorithmBenchmarkResult(
                "Naive (Brute Force)",
                matchesFound,
                comparisonsCount,
                executionTimeNano,
                "O(N*M)",
                "O(1)"
        );
    }
}
