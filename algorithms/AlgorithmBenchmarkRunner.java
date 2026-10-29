package algorithms;

import model.AlgorithmBenchmarkResult;
import util.MyArrayList;

public class AlgorithmBenchmarkRunner {

    private static final MyArrayList<PatternMatcher> MATCHERS = new MyArrayList<>();

    static {
        MATCHERS.add(new NaivePatternMatcher());
        MATCHERS.add(new KmpPatternMatcher());
        MATCHERS.add(new ZFunctionPatternMatcher());
        MATCHERS.add(new RabinKarpPatternMatcher());
        MATCHERS.add(new AhoCorasickPatternMatcher());
        MATCHERS.add(new KasaiLcpPatternMatcher());
    }

    public static MyArrayList<PatternMatcher> getMatchers() {
        return MATCHERS;
    }

    public static AlgorithmBenchmarkResult runMatcher(int index, String text, String pattern) {
        if (index < 0 || index >= MATCHERS.size()) {
            throw new IllegalArgumentException("Invalid matcher index: " + index);
        }
        return MATCHERS.get(index).search(text, pattern);
    }

    public static MyArrayList<AlgorithmBenchmarkResult> runAll(String text, String pattern) {
        MyArrayList<AlgorithmBenchmarkResult> results = new MyArrayList<>();
        for (int i = 0; i < MATCHERS.size(); i++) {
            PatternMatcher matcher = MATCHERS.get(i);
            matcher.search(text, pattern); 
            AlgorithmBenchmarkResult res = matcher.search(text, pattern);
            results.add(res);
        }
        return results;
    }
}
