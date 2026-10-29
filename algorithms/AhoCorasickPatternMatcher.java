package algorithms;

import model.AlgorithmBenchmarkResult;
import util.MyQueue;

public class AhoCorasickPatternMatcher implements PatternMatcher {

    private static class Node {
        Node[] children = new Node[256];
        Node failure = null;
        boolean isEndOfWord = false;
    }

    @Override
    public AlgorithmBenchmarkResult search(String text, String pattern) {
        long startTime = System.nanoTime();
        int matchesFound = 0;
        long comparisonsCount = 0;

        if (text == null || pattern == null) {
            long duration = System.nanoTime() - startTime;
            return new AlgorithmBenchmarkResult("Aho-Corasick", 0, 0, duration, "O(N+M)", "O(M)");
        }

        int n = text.length();
        int m = pattern.length();

        if (m == 0 || n < m) {
            long duration = System.nanoTime() - startTime;
            return new AlgorithmBenchmarkResult("Aho-Corasick", 0, 0, duration, "O(N+M)", "O(M)");
        }

        Node root = new Node();
        Node curr = root;
        for (int i = 0; i < m; i++) {
            char c = pattern.charAt(i);
            int idx = (int) c;
            if (idx >= 0 && idx < 256) {
                if (curr.children[idx] == null) {
                    curr.children[idx] = new Node();
                }
                curr = curr.children[idx];
            }
        }
        curr.isEndOfWord = true;

        MyQueue<Node> queue = new MyQueue<>();
        for (int idx = 0; idx < 256; idx++) {
            Node child = root.children[idx];
            if (child != null) {
                child.failure = root;
                queue.enqueue(child);
            }
        }

        while (!queue.isEmpty()) {
            Node r = queue.dequeue();
            for (int idx = 0; idx < 256; idx++) {
                Node child = r.children[idx];
                if (child != null) {
                    Node f = r.failure;
                    while (f != null && f.children[idx] == null) {
                        comparisonsCount++;
                        f = f.failure;
                    }
                    if (f == null) {
                        child.failure = root;
                    } else {
                        child.failure = f.children[idx];
                        comparisonsCount++;
                    }
                    queue.enqueue(child);
                }
            }
        }

        curr = root;
        for (int i = 0; i < n; i++) {
            char c = text.charAt(i);
            int idx = (int) c;
            if (idx < 0 || idx >= 256) {
                curr = root;
                continue;
            }
            while (curr != null && curr.children[idx] == null) {
                comparisonsCount++;
                curr = curr.failure;
            }
            if (curr == null) {
                curr = root;
            } else {
                curr = curr.children[idx];
                comparisonsCount++;
                
                Node temp = curr;
                while (temp != root && temp != null) {
                    if (temp.isEndOfWord) {
                        matchesFound++;
                    }
                    temp = temp.failure;
                }
            }
        }

        long endTime = System.nanoTime();
        long executionTimeNano = endTime - startTime;

        return new AlgorithmBenchmarkResult(
                "Aho-Corasick",
                matchesFound,
                comparisonsCount,
                executionTimeNano,
                "O(N+M)",
                "O(M)"
            );
    }
}
