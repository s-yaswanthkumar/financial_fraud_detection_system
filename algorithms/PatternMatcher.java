package algorithms;

import model.AlgorithmBenchmarkResult;

public interface PatternMatcher {
    AlgorithmBenchmarkResult search(String text, String pattern);
}
