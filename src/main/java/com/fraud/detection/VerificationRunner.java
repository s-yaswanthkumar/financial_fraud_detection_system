package com.fraud.detection;

import com.fraud.detection.algorithms.*;
import com.fraud.detection.engine.FraudRiskEngine;
import com.fraud.detection.model.*;
import com.fraud.detection.storage.CsvFileStorageServiceImpl;
import com.fraud.detection.util.MyArrayList;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;

public class VerificationRunner {

    public static void main(String[] args) {
        System.out.println("==========================================================");
        System.out.println("            RUNNING SYSTEM VERIFICATION TESTS             ");
        System.out.println("==========================================================");

        boolean allAlgorithmsPass = testPatternMatchers();
        boolean riskEnginePass = testRiskEngine();

        System.out.println("\n==========================================================");
        if (allAlgorithmsPass && riskEnginePass) {
            System.out.println("        STATUS: ALL SYSTEM TESTS PASSED SUCCESSFULLY!    ");
        } else {
            System.out.println("        STATUS: SOME TESTS FAILED! PLEASE REVIEW LOGS.   ");
        }
        System.out.println("==========================================================");
        
        try {
            Files.deleteIfExists(Paths.get("data/test_users.csv"));
            Files.deleteIfExists(Paths.get("data/test_transactions.csv"));
        } catch (IOException e) {
        }
    }

    private static boolean testPatternMatchers() {
        System.out.println("\n[TEST] Verifying Pattern Matching Algorithms...");
        String text = "abacadabrabracabscamabacabscam";
        String pattern = "scam";
        int expectedMatches = 2;

        MyArrayList<PatternMatcher> matchers = new MyArrayList<>();
        matchers.add(new NaivePatternMatcher());
        matchers.add(new KmpPatternMatcher());
        matchers.add(new ZFunctionPatternMatcher());
        matchers.add(new RabinKarpPatternMatcher());
        matchers.add(new AhoCorasickPatternMatcher());
        matchers.add(new KasaiLcpPatternMatcher());

        boolean allPass = true;
        for (int i = 0; i < matchers.size(); i++) {
            PatternMatcher matcher = matchers.get(i);
            AlgorithmBenchmarkResult res = matcher.search(text, pattern);
            boolean pass = (res.getMatchesFound() == expectedMatches);
            System.out.printf("  - %-25s: Matches Found = %d (Expected = %d) | Comparisons = %d | Status = %s\n",
                    res.getAlgorithmName(),
                    res.getMatchesFound(),
                    expectedMatches,
                    res.getComparisonsCount(),
                    pass ? "PASS" : "FAIL"
            );
            if (!pass) {
                allPass = false;
            }
        }
        return allPass;
    }

    private static boolean testRiskEngine() {
        System.out.println("\n[TEST] Verifying Fraud Risk Assessment Engine...");

        CsvFileStorageServiceImpl testStorage = new CsvFileStorageServiceImpl(
                "data/test_users.csv",
                "data/test_transactions.csv"
        );

        MyArrayList<User> testUsers = new MyArrayList<>();
        testUsers.add(new User("U201", "test_alice", "pass", 1000.0, LocalDateTime.now()));
        testUsers.add(new User("U202", "test_bob", "pass", 1000.0, LocalDateTime.now()));
        testStorage.saveUsers(testUsers);
        testStorage.saveTransactions(new MyArrayList<>());

        FraudRiskEngine testEngine = new FraudRiskEngine(testStorage);
        PatternMatcher matcher = new KmpPatternMatcher();

        boolean allPass = true;

        Transaction normalTx = new Transaction("T2001", "U201", "U202", 50.0, "Ordinary transfer", LocalDateTime.now(), 0, "PENDING");
        RiskAssessment r1 = testEngine.assessTransaction(normalTx, matcher);
        boolean tc1 = (r1.getFinalScore() == 0 && "APPROVED".equalsIgnoreCase(r1.getRiskTier()));
        System.out.printf("  - Test Case 1 (Normal Tx)     : Score = %.1f, Tier = %s | Expected = 0.0, APPROVED | Status = %s\n",
                r1.getFinalScore(), r1.getRiskTier(), tc1 ? "PASS" : "FAIL");
        allPass = allPass && tc1;
        
        normalTx.setStatus("APPROVED");
        testStorage.addTransaction(normalTx);

        Transaction deviationTx = new Transaction("T2002", "U201", "U202", 200.0, "Buying electronics", LocalDateTime.now().plusMinutes(1), 0, "PENDING");
        RiskAssessment r2 = testEngine.assessTransaction(deviationTx, matcher);
        boolean tc2 = (r2.getFinalScore() == 20 && r2.getRiskFactorsList().size() == 1);
        System.out.printf("  - Test Case 2 (Deviation Tx)  : Score = %.1f | Expected = 20.0 | Status = %s\n",
                r2.getFinalScore(), tc2 ? "PASS" : "FAIL");
        allPass = allPass && tc2;

        Transaction blacklistTx = new Transaction("T2003", "U201", "U202", 60.0, "sending wire transfer scam money", LocalDateTime.now().plusMinutes(2), 0, "PENDING");
        RiskAssessment r3 = testEngine.assessTransaction(blacklistTx, matcher);
        boolean tc3 = (r3.getFinalScore() == 25);
        System.out.printf("  - Test Case 3 (Blacklist Memo): Score = %.1f | Expected = 25.0 | Status = %s\n",
                r3.getFinalScore(), tc3 ? "PASS" : "FAIL");
        allPass = allPass && tc3;

        Transaction duplicateTx = new Transaction("T2004", "U201", "U202", 50.0, "Ordinary transfer duplicate", LocalDateTime.now().plusMinutes(3), 0, "PENDING");
        RiskAssessment r4 = testEngine.assessTransaction(duplicateTx, matcher);
        boolean tc4 = (r4.getFinalScore() == 30);
        System.out.printf("  - Test Case 4 (Duplicate Tx)  : Score = %.1f | Expected = 30.0 | Status = %s\n",
                r4.getFinalScore(), tc4 ? "PASS" : "FAIL");
        allPass = allPass && tc4;
        
        duplicateTx.setStatus("APPROVED");
        testStorage.addTransaction(duplicateTx);

        Transaction comboTx = new Transaction("T2005", "U201", "U202", 250.0, "phishing scam wire payment", LocalDateTime.now().plusMinutes(4), 0, "PENDING");
        RiskAssessment r5 = testEngine.assessTransaction(comboTx, matcher);
        boolean tc5 = (r5.getFinalScore() == 70 && "BLOCKED".equalsIgnoreCase(r5.getRiskTier()));
        System.out.printf("  - Test Case 5 (Combo Trigger) : Score = %.1f, Tier = %s | Expected = 70.0, BLOCKED | Status = %s\n",
                r5.getFinalScore(), r5.getRiskTier(), tc5 ? "PASS" : "FAIL");
        allPass = allPass && tc5;

        return allPass;
    }
}
