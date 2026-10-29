package algorithms;

import model.AlgorithmBenchmarkResult;

public class ZFunctionPatternMatcher implements PatternMatcher {

    @Override
    public AlgorithmBenchmarkResult search(String text, String pattern) {
        long startTime = System.nanoTime();
        int matchesFound = 0;
        long comparisonsCount = 0;

        if (text == null || pattern == null) {
            long duration = System.nanoTime() - startTime;
            return new AlgorithmBenchmarkResult("Z-Function", 0, 0, duration, "O(N+M)", "O(N+M)");
        }

        int n = text.length();
        int m = pattern.length();

        if (m == 0 || n < m) {
            long duration = System.nanoTime() - startTime;
            return new AlgorithmBenchmarkResult("Z-Function", 0, 0, duration, "O(N+M)", "O(N+M)");
        }

        String concat = pattern + "$" + text;
        int len = concat.length();
        int[] z = new int[len];
        
        int l = 0;
        int r = 0;
        
        for (int i = 1; i < len; i++) {
            if (i > r) {
                l = r = i;
                while (r < len) {
                    comparisonsCount++;
                    if (concat.charAt(r - l) == concat.charAt(r)) {
                        r++;
                    } else {
                        break;
                    }
                }
                z[i] = r - l;
                r--;
            } else {
                int k = i - l;
                if (z[k] < r - i + 1) {
                    z[i] = z[k];
                } else {
                    l = i;
                    while (r < len) {
                        comparisonsCount++;
                        if (concat.charAt(r - l) == concat.charAt(r)) {
                            r++;
                        } else {
                            break;
                        }
                    }
                    z[i] = r - l;
                    r--;
                }
            }
        }

        for (int i = m + 1; i < len; i++) {
            if (z[i] == m) {
                matchesFound++;
            }
        }

        long endTime = System.nanoTime();
        long executionTimeNano = endTime - startTime;

        return new AlgorithmBenchmarkResult(
                "Z-Function",
                matchesFound,
                comparisonsCount,
                executionTimeNano,
                "O(N+M)",
                "O(N+M)"
        );
    }
}
