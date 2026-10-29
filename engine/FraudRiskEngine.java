package engine;

import algorithms.PatternMatcher;
import model.RiskAssessment;
import model.Transaction;
import storage.FileStorageService;
import util.MyArrayList;

import java.time.LocalDateTime;

public class FraudRiskEngine {
    private final FileStorageService storageService;

    private static final MyArrayList<String> BLACKLISTED_SIGNATURES = new MyArrayList<>();

    static {
        BLACKLISTED_SIGNATURES.add("money laundering");
        BLACKLISTED_SIGNATURES.add("tax evasion");
        BLACKLISTED_SIGNATURES.add("shell company");
        BLACKLISTED_SIGNATURES.add("phishing");
        BLACKLISTED_SIGNATURES.add("scam");
        BLACKLISTED_SIGNATURES.add("mule account");
        BLACKLISTED_SIGNATURES.add("offshore bank");
        BLACKLISTED_SIGNATURES.add("illicit drugs");
        BLACKLISTED_SIGNATURES.add("anonymous cash");
        BLACKLISTED_SIGNATURES.add("wire transfer scam");
    }

    public FraudRiskEngine(FileStorageService storageService) {
        this.storageService = storageService;
    }

    public RiskAssessment assessTransaction(Transaction currentTx, PatternMatcher matcher) {
        double score = 0;
        MyArrayList<String> factors = new MyArrayList<>();

        MyArrayList<Transaction> history = storageService.loadTransactions();
        LocalDateTime currentTxTime = currentTx.getTimestamp();

        long velocityCount = 0;
        for (int i = 0; i < history.size(); i++) {
            Transaction tx = history.get(i);
            if (tx.getSenderId().equalsIgnoreCase(currentTx.getSenderId())) {
                if (!"BLOCKED".equalsIgnoreCase(tx.getStatus()) && !"FAILED".equalsIgnoreCase(tx.getStatus())) {
                    if (tx.getTimestamp().isAfter(currentTxTime.minusMinutes(10)) && !tx.getTimestamp().isAfter(currentTxTime)) {
                        velocityCount++;
                    }
                }
            }
        }

        if (velocityCount + 1 >= 3) {
            score += 25;
            factors.add(String.format("Velocity Spike: Detected %d transactions in last 10 minutes (+25)", velocityCount + 1));
        }

        double totalAmount = 0;
        int senderTxCount = 0;
        for (int i = 0; i < history.size(); i++) {
            Transaction tx = history.get(i);
            if (tx.getSenderId().equalsIgnoreCase(currentTx.getSenderId())) {
                if (!"BLOCKED".equalsIgnoreCase(tx.getStatus()) && !"FAILED".equalsIgnoreCase(tx.getStatus())) {
                    totalAmount += tx.getAmount();
                    senderTxCount++;
                }
            }
        }

        if (senderTxCount > 0) {
            double avgAmount = totalAmount / senderTxCount;
            if (currentTx.getAmount() > 3 * avgAmount) {
                score += 20;
                factors.add(String.format("Historical Deviation: Amount $%.2f is > 3x average ($%.2f) (+20)", currentTx.getAmount(), avgAmount));
            }
        }

        boolean duplicateFound = false;
        for (int i = 0; i < history.size(); i++) {
            Transaction tx = history.get(i);
            if (tx.getSenderId().equalsIgnoreCase(currentTx.getSenderId())
                    && tx.getReceiverId().equalsIgnoreCase(currentTx.getReceiverId())
                    && Math.abs(tx.getAmount() - currentTx.getAmount()) < 0.0001) {
                if (!"BLOCKED".equalsIgnoreCase(tx.getStatus()) && !"FAILED".equalsIgnoreCase(tx.getStatus())) {
                    if (tx.getTimestamp().isAfter(currentTxTime.minusMinutes(5)) && !tx.getTimestamp().isAfter(currentTxTime)) {
                        duplicateFound = true;
                        break;
                    }
                }
            }
        }

        if (duplicateFound) {
            score += 30;
            factors.add("Rapid Duplicate: Identical transaction found within last 5 minutes (+30)");
        }

        boolean signatureFound = false;
        String memoLower = currentTx.getMemo().toLowerCase();
        for (int i = 0; i < BLACKLISTED_SIGNATURES.size(); i++) {
            String signature = BLACKLISTED_SIGNATURES.get(i);
            var res = matcher.search(memoLower, signature);
            if (res.getMatchesFound() > 0) {
                signatureFound = true;
                factors.add(String.format("Fraud Signature Detected: Found blacklisted term \"%s\" (+25)", signature));
                break;
            }
        }
        if (signatureFound) {
            score += 25;
        }

        String tier;
        boolean isBlocked = false;
        if (score >= 70) {
            tier = "BLOCKED";
            isBlocked = true;
        } else if (score >= 40) {
            tier = "FLAGGED/OTP";
        } else {
            tier = "APPROVED";
        }

        return new RiskAssessment(score, tier, factors, isBlocked);
    }
}
