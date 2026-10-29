package algorithms;

import model.AlgorithmBenchmarkResult;

public class KmpPatternMatcher implements PatternMatcher {

    @Override
    public AlgorithmBenchmarkResult search(String text, String pattern) {
        long startTime = System.nanoTime();
        int matchesFound = 0;
        long comparisonsCount = 0;

        if (text == null || pattern == null) {
            long duration = System.nanoTime() - startTime;
            return new AlgorithmBenchmarkResult("KMP", 0, 0, duration, "O(N+M)", "O(M)");
        }

        int n = text.length();
        int m = pattern.length();

        if (m == 0 || n < m) {
            long duration = System.nanoTime() - startTime;
            return new AlgorithmBenchmarkResult("KMP", 0, 0, duration, "O(N+M)", "O(M)");
        }

        int[] lps = new int[m];
        int j = 0;
        lps[0] = 0;
        int i = 1;
        while (i < m) {
            comparisonsCount++;
            if (pattern.charAt(i) == pattern.charAt(j)) {
                j++;
                lps[i] = j;
                i++;
            } else {
                if (j != 0) {
                    j = lps[j - 1];
                } else {
                    lps[i] = 0;
                    i++;
                }
            }
        }

        int txtIdx = 0;
        int patIdx = 0;
        while (txtIdx < n) {
            comparisonsCount++;
            if (pattern.charAt(patIdx) == text.charAt(txtIdx)) {
                patIdx++;
                txtIdx++;
                if (patIdx == m) {
                    matchesFound++;
                    patIdx = lps[patIdx - 1];
                }
            } else {
                if (patIdx != 0) {
                    patIdx = lps[patIdx - 1];
                } else {
                    txtIdx++;
                }
            }
        }

        long endTime = System.nanoTime();
        long executionTimeNano = endTime - startTime;

        return new AlgorithmBenchmarkResult(
                "Knuth-Morris-Pratt",
                matchesFound,
                comparisonsCount,
                executionTimeNano,
                "O(N+M)",
                "O(M)"
        );
    }
}
