package algorithms;

import model.AlgorithmBenchmarkResult;

public class KasaiLcpPatternMatcher implements PatternMatcher {

    @Override
    public AlgorithmBenchmarkResult search(String text, String pattern) {
        long startTime = System.nanoTime();
        int matchesFound = 0;
        long[] comparisonsCount = new long[]{0};

        if (text == null || pattern == null) {
            long duration = System.nanoTime() - startTime;
            return new AlgorithmBenchmarkResult("Kasai Suffix Array", 0, 0, duration, "O(N log N + M log N)", "O(N)");
        }

        int n = text.length();
        int m = pattern.length();

        if (m == 0 || n < m) {
            long duration = System.nanoTime() - startTime;
            return new AlgorithmBenchmarkResult("Kasai Suffix Array", 0, 0, duration, "O(N log N + M log N)", "O(N)");
        }

        int[] sa = new int[n];
        for (int i = 0; i < n; i++) {
            sa[i] = i;
        }

        quickSort(sa, 0, n - 1, text, comparisonsCount);

        int[] lcp = new int[n];
        int[] rank = new int[n];
        for (int i = 0; i < n; i++) {
            rank[sa[i]] = i;
        }

        int k = 0;
        for (int i = 0; i < n; i++) {
            if (rank[i] == 0) {
                lcp[0] = 0;
                k = 0;
                continue;
            }
            int j = sa[rank[i] - 1];
            while (i + k < n && j + k < n) {
                comparisonsCount[0]++;
                if (text.charAt(i + k) == text.charAt(j + k)) {
                    k++;
                } else {
                    break;
                }
            }
            lcp[rank[i]] = k;
            if (k > 0) {
                k--;
            }
        }

        int low = 0;
        int high = n - 1;
        int matchIndex = -1;

        while (low <= high) {
            int mid = (low + high) >>> 1;
            int cmp = comparePatternWithSuffix(text, pattern, sa[mid], comparisonsCount);
            if (cmp == 0) {
                matchIndex = mid;
                break;
            } else if (cmp < 0) {
                high = mid - 1;
            } else {
                low = mid + 1;
            }
        }

        if (matchIndex != -1) {
            matchesFound = 1;
            
            int left = matchIndex - 1;
            while (left >= 0) {
                int cmp = comparePatternWithSuffix(text, pattern, sa[left], comparisonsCount);
                if (cmp == 0) {
                    matchesFound++;
                    left--;
                } else {
                    break;
                }
            }

            int right = matchIndex + 1;
            while (right < n) {
                int cmp = comparePatternWithSuffix(text, pattern, sa[right], comparisonsCount);
                if (cmp == 0) {
                    matchesFound++;
                    right++;
                } else {
                    break;
                }
            }
        }

        long endTime = System.nanoTime();
        long executionTimeNano = endTime - startTime;

        return new AlgorithmBenchmarkResult(
                "Kasai Suffix Array",
                matchesFound,
                comparisonsCount[0],
                executionTimeNano,
                "O(N log N + M log N)",
                "O(N)"
        );
    }

    private void quickSort(int[] sa, int low, int high, String text, long[] comparisonsCount) {
        if (low < high) {
            int pi = partition(sa, low, high, text, comparisonsCount);
            quickSort(sa, low, pi - 1, text, comparisonsCount);
            quickSort(sa, pi + 1, high, text, comparisonsCount);
        }
    }

    private int partition(int[] sa, int low, int high, String text, long[] comparisonsCount) {
        int pivot = sa[high];
        int i = (low - 1);
        for (int j = low; j < high; j++) {
            if (compareSuffixes(text, sa[j], pivot, comparisonsCount) < 0) {
                i++;
                int temp = sa[i];
                sa[i] = sa[j];
                sa[j] = temp;
            }
        }
        int temp = sa[i + 1];
        sa[i + 1] = sa[high];
        sa[high] = temp;
        return i + 1;
    }

    private int compareSuffixes(String text, int a, int b, long[] comparisonsCount) {
        int n = text.length();
        int lenA = n - a;
        int lenB = n - b;
        int limit = Math.min(lenA, lenB);
        for (int k = 0; k < limit; k++) {
            comparisonsCount[0]++;
            char c1 = text.charAt(a + k);
            char c2 = text.charAt(b + k);
            if (c1 != c2) {
                return Character.compare(c1, c2);
            }
        }
        return Integer.compare(lenA, lenB);
    }

    private int comparePatternWithSuffix(String text, String pattern, int suffixStart, long[] comparisonsCount) {
        int m = pattern.length();
        int n = text.length();
        int suffixLen = n - suffixStart;
        int limit = Math.min(m, suffixLen);

        for (int i = 0; i < limit; i++) {
            comparisonsCount[0]++;
            char c1 = pattern.charAt(i);
            char c2 = text.charAt(suffixStart + i);
            if (c1 != c2) {
                return Character.compare(c1, c2);
            }
        }

        if (limit < m) {
            return 1;
        }

        return 0;
    }
}
