package com.fraud.detection.algorithms;

import com.fraud.detection.model.AlgorithmBenchmarkResult;

public class RabinKarpPatternMatcher implements PatternMatcher {

    private static final long BASE = 256;
    private static final long PRIME = 1000000007;

    @Override
    public AlgorithmBenchmarkResult search(String text, String pattern) {
        long startTime = System.nanoTime();
        int matchesFound = 0;
        long comparisonsCount = 0;

        if (text == null || pattern == null) {
            long duration = System.nanoTime() - startTime;
            return new AlgorithmBenchmarkResult("Rabin-Karp", 0, 0, duration, "O(N+M)", "O(1)");
        }

        int n = text.length();
        int m = pattern.length();

        if (m == 0 || n < m) {
            long duration = System.nanoTime() - startTime;
            return new AlgorithmBenchmarkResult("Rabin-Karp", 0, 0, duration, "O(N+M)", "O(1)");
        }

        long pHash = 0;
        long tHash = 0;
        long h = 1;

        for (int i = 0; i < m - 1; i++) {
            h = (h * BASE) % PRIME;
        }

        for (int i = 0; i < m; i++) {
            pHash = (BASE * pHash + pattern.charAt(i)) % PRIME;
            tHash = (BASE * tHash + text.charAt(i)) % PRIME;
        }

        for (int i = 0; i <= n - m; i++) {
            if (pHash == tHash) {
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

            if (i < n - m) {
                long term = (text.charAt(i) * h) % PRIME;
                long diff = (tHash - term + PRIME) % PRIME;
                tHash = (diff * BASE + text.charAt(i + m)) % PRIME;
            }
        }

        long endTime = System.nanoTime();
        long executionTimeNano = endTime - startTime;

        return new AlgorithmBenchmarkResult(
                "Rabin-Karp",
                matchesFound,
                comparisonsCount,
                executionTimeNano,
                "O(N+M)",
                "O(1)"
        );
    }
}
