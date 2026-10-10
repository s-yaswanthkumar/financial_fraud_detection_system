package com.fraud.detection.algorithms;

import com.fraud.detection.model.AlgorithmBenchmarkResult;

public interface PatternMatcher {
    AlgorithmBenchmarkResult search(String text, String pattern);
}
