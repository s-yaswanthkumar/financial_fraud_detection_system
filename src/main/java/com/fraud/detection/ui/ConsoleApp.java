package com.fraud.detection.ui;

import com.fraud.detection.algorithms.AlgorithmBenchmarkRunner;
import com.fraud.detection.algorithms.PatternMatcher;
import com.fraud.detection.auth.AuthService;
import com.fraud.detection.engine.FraudRiskEngine;
import com.fraud.detection.model.AlgorithmBenchmarkResult;
import com.fraud.detection.model.RiskAssessment;
import com.fraud.detection.model.Transaction;
import com.fraud.detection.model.User;
import com.fraud.detection.storage.CsvFileStorageServiceImpl;
import com.fraud.detection.storage.FileStorageService;
import com.fraud.detection.util.MyArrayList;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class ConsoleApp {

    private static FileStorageService storageService;
    private static AuthService authService;
    private static FraudRiskEngine riskEngine;

    public static void main(String[] args) {
        storageService = new CsvFileStorageServiceImpl();
        authService = new AuthService(storageService);
        riskEngine = new FraudRiskEngine(storageService);

        Scanner scanner = new Scanner(System.in);
        System.out.println("=================================================================");
        System.out.println("   WELCOME TO THE FINANCIAL FRAUD DETECTION SYSTEM (ACADEMIC 2)  ");
        System.out.println("=================================================================");

        while (true) {
            if (authService.getCurrentUser() == null) {
                showWelcomeMenu(scanner);
            } else {
                showDashboard(scanner);
            }
        }
    }

    private static void showWelcomeMenu(Scanner scanner) {
        System.out.println("\n--- Welcome Menu ---");
        System.out.println("1. Login");
        System.out.println("2. Register");
        System.out.println("3. Exit System");
        System.out.print("Select an option: ");

        int choice = getIntInput(scanner);
        switch (choice) {
            case 1:
                handleLogin(scanner);
                break;
            case 2:
                handleRegister(scanner);
                break;
            case 3:
                System.out.println("\nThank you for using the Financial Fraud Detection System. Goodbye!");
                System.exit(0);
                break;
            default:
                System.out.println("Invalid choice. Please select 1, 2, or 3.");
        }
    }

    private static void showDashboard(Scanner scanner) {
        authService.refreshCurrentUser();
        User user = authService.getCurrentUser();
        System.out.println("\n=================================================================");
        System.out.printf("  USER DASHBOARD | User: %s (%s) | Balance: $%.2f\n", 
                user.getUsername(), user.getUserId(), user.getBalance());
        System.out.println("=================================================================");
        System.out.println("1. Transfer Funds (Simulate Transaction)");
        System.out.println("2. View Transaction History");
        System.out.println("3. Run Algorithm Benchmark Suite");
        System.out.println("4. Check Balance");
        System.out.println("5. Logout");
        System.out.print("Select an option: ");

        int choice = getIntInput(scanner);
        switch (choice) {
            case 1:
                handleTransferSimulation(scanner);
                break;
            case 2:
                handleViewHistory();
                break;
            case 3:
                handleBenchmarkSuite(scanner);
                break;
            case 4:
                System.out.printf("\nYour current balance is: $%.2f\n", authService.getCurrentUser().getBalance());
                break;
            case 5:
                authService.logout();
                System.out.println("\nLogged out successfully.");
                break;
            default:
                System.out.println("Invalid choice. Please select from 1 to 5.");
        }
    }

    private static void handleLogin(Scanner scanner) {
        System.out.println("\n--- User Login ---");
        System.out.print("Username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine().trim();

        User user = authService.login(username, password);
        if (user != null) {
            System.out.printf("\nWelcome back, %s! Access granted.\n", user.getUsername());
        } else {
            System.out.println("\n[ERROR] Invalid username or password. Access denied.");
        }
    }

    private static void handleRegister(Scanner scanner) {
        System.out.println("\n--- User Registration ---");
        System.out.print("Enter username: ");
        String username = scanner.nextLine().trim();
        if (username.isEmpty()) {
            System.out.println("[ERROR] Username cannot be empty.");
            return;
        }
        System.out.print("Enter password: ");
        String password = scanner.nextLine().trim();
        if (password.isEmpty()) {
            System.out.println("[ERROR] Password cannot be empty.");
            return;
        }
        System.out.print("Enter initial deposit balance: $");
        double balance = getDoubleInput(scanner);
        if (balance < 0) {
            System.out.println("[ERROR] Initial balance cannot be negative.");
            return;
        }

        boolean success = authService.register(username, password, balance);
        if (success) {
            System.out.println("\nRegistration successful! You can now log in.");
        } else {
            System.out.println("\n[ERROR] Username already exists. Please choose a different one.");
        }
    }

    private static void handleTransferSimulation(Scanner scanner) {
        User sender = authService.getCurrentUser();
        System.out.println("\n--- Fund Transfer Simulator ---");
        System.out.print("Enter Recipient Username or User ID: ");
        String recipientInput = scanner.nextLine().trim();

        User receiver = storageService.findUserByUsername(recipientInput);
        if (receiver == null) {
            receiver = storageService.findUserById(recipientInput);
        }

        if (receiver == null) {
            System.out.println("[ERROR] Recipient not found in the system database.");
            return;
        }

        if (receiver.getUserId().equalsIgnoreCase(sender.getUserId())) {
            System.out.println("[ERROR] You cannot transfer funds to yourself.");
            return;
        }

        System.out.print("Enter Transfer Amount: $");
        double amount = getDoubleInput(scanner);
        if (amount <= 0) {
            System.out.println("[ERROR] Transfer amount must be positive.");
            return;
        }

        if (sender.getBalance() < amount) {
            System.out.printf("[ERROR] Insufficient funds. Available balance: $%.2f\n", sender.getBalance());
            return;
        }

        System.out.print("Enter Memo / Payment Note: ");
        String memo = scanner.nextLine().trim();

        System.out.println("\nSelect Memo Analysis Matching Algorithm:");
        MyArrayList<PatternMatcher> matchers = AlgorithmBenchmarkRunner.getMatchers();
        for (int i = 0; i < matchers.size(); i++) {
            System.out.printf("  %d. %s\n", (i + 1), matchers.get(i).getClass().getSimpleName().replace("PatternMatcher", ""));
        }
        System.out.print("Enter selector (1-6): ");
        int algChoice = getIntInput(scanner);
        if (algChoice < 1 || algChoice > matchers.size()) {
            System.out.println("[ERROR] Invalid algorithm selection. Aborting transaction.");
            return;
        }
        PatternMatcher selectedMatcher = matchers.get(algChoice - 1);

        int txIdSuffix = storageService.loadTransactions().size() + 1001;
        String txId = "TX" + txIdSuffix;
        Transaction transaction = new Transaction(
                txId,
                sender.getUserId(),
                receiver.getUserId(),
                amount,
                memo,
                LocalDateTime.now(),
                0.0,
                "PENDING"
        );

        System.out.println("\n[ENGINE] Evaluating fraud risk profile...");
        RiskAssessment assessment = riskEngine.assessTransaction(transaction, selectedMatcher);
        transaction.setRiskScore(assessment.getFinalScore());

        System.out.println("\n=================================================================");
        System.out.println("                    REAL-TIME RISK ASSESSMENT REPORT             ");
        System.out.println("=================================================================");
        System.out.printf("  Transaction ID    : %s\n", txId);
        System.out.printf("  Sender ID         : %s\n", sender.getUserId());
        System.out.printf("  Receiver ID       : %s\n", receiver.getUserId());
        System.out.printf("  Amount            : $%.2f\n", amount);
        System.out.printf("  Risk Score (0-100): %.1f\n", assessment.getFinalScore());
        System.out.printf("  Risk Tier Level   : %s\n", assessment.getRiskTier());
        System.out.println("  Risk Trigger Factors:");
        MyArrayList<String> factors = assessment.getRiskFactorsList();
        if (factors.isEmpty()) {
            System.out.println("    - None (Clean Transaction Profile)");
        } else {
            for (int i = 0; i < factors.size(); i++) {
                System.out.printf("    - %s\n", factors.get(i));
            }
        }
        System.out.println("=================================================================");

        if ("APPROVED".equalsIgnoreCase(assessment.getRiskTier())) {
            executeTransfer(sender, receiver, amount, transaction, "APPROVED");
        } else if ("FLAGGED/OTP".equalsIgnoreCase(assessment.getRiskTier())) {
            System.out.println("\n[SECURITY] Verification Required. Out-of-Band OTP sent to device.");
            System.out.print("Enter 6-digit verification code (Hint: 123456): ");
            String otp = scanner.nextLine().trim();
            if ("123456".equals(otp)) {
                System.out.println("[SUCCESS] Verification code approved.");
                executeTransfer(sender, receiver, amount, transaction, "APPROVED");
            } else {
                System.out.println("[DENIED] OTP verification failed. Transaction rejected.");
                transaction.setStatus("FAILED");
                storageService.addTransaction(transaction);
            }
        } else {
            System.out.println("\n[ALERT] Transaction BLOCKED by system engine due to high security risk level.");
            transaction.setStatus("BLOCKED");
            storageService.addTransaction(transaction);
        }
    }

    private static void executeTransfer(User sender, User receiver, double amount, Transaction tx, String status) {
        sender.setBalance(sender.getBalance() - amount);
        receiver.setBalance(receiver.getBalance() + amount);

        MyArrayList<User> users = storageService.loadUsers();
        for (int i = 0; i < users.size(); i++) {
            User u = users.get(i);
            if (u.getUserId().equalsIgnoreCase(sender.getUserId())) {
                u.setBalance(sender.getBalance());
            } else if (u.getUserId().equalsIgnoreCase(receiver.getUserId())) {
                u.setBalance(receiver.getBalance());
            }
        }
        storageService.saveUsers(users);

        tx.setStatus(status);
        storageService.addTransaction(tx);
        System.out.printf("\n[SUCCESS] Transaction %s processed successfully!\n$%.2f transferred to %s.\n",
                tx.getTxId(), amount, receiver.getUsername());
    }

    private static void handleViewHistory() {
        User current = authService.getCurrentUser();
        MyArrayList<Transaction> allTxs = storageService.loadTransactions();
        MyArrayList<Transaction> myTxs = new MyArrayList<>();
        for (int i = 0; i < allTxs.size(); i++) {
            Transaction tx = allTxs.get(i);
            if (tx.getSenderId().equalsIgnoreCase(current.getUserId()) 
                    || tx.getReceiverId().equalsIgnoreCase(current.getUserId())) {
                myTxs.add(tx);
            }
        }

        System.out.println("\n======================================= TRANSACTION HISTORY =======================================");
        if (myTxs.isEmpty()) {
            System.out.println("  No transactions recorded for this account.");
            System.out.println("====================================================================================================");
            return;
        }

        System.out.printf("| %-8s | %-8s | %-8s | %-11s | %-25s | %-19s | %-10s | %-10s |\n",
                "TX ID", "Sender", "Receiver", "Amount", "Memo", "Timestamp", "Risk Score", "Status");
        System.out.println("----------------------------------------------------------------------------------------------------");

        for (int i = 0; i < myTxs.size(); i++) {
            Transaction tx = myTxs.get(i);
            User sender = storageService.findUserById(tx.getSenderId());
            User receiver = storageService.findUserById(tx.getReceiverId());

            String senderName = sender != null ? sender.getUsername() : tx.getSenderId();
            String receiverName = receiver != null ? receiver.getUsername() : tx.getReceiverId();

            String displayMemo = tx.getMemo();
            if (displayMemo.length() > 25) {
                displayMemo = displayMemo.substring(0, 22) + "...";
            }

            System.out.printf("| %-8s | %-8s | %-8s | $%-10.2f | %-25s | %-19s | %-10.1f | %-10s |\n",
                    tx.getTxId(),
                    senderName,
                    receiverName,
                    tx.getAmount(),
                    displayMemo,
                    tx.getTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                    tx.getRiskScore(),
                    tx.getStatus()
            );
        }
        System.out.println("====================================================================================================");
    }

    private static void handleBenchmarkSuite(Scanner scanner) {
        System.out.println("\n--- Algorithm Benchmark Runner ---");
        System.out.println("Enter text to search (Press Enter for default academic security document):");
        String text = scanner.nextLine().trim();
        if (text.isEmpty()) {
            text = "The suspect was caught conducting illegal money laundering activities. Investigators found evidence "
                 + "of a tax evasion scheme using an offshore bank account, a shell company, and a mule account to run "
                 + "a phishing scan and wire transfer scam.";
        }

        System.out.println("Enter pattern to search (Press Enter for default 'scam'):");
        String pattern = scanner.nextLine().trim();
        if (pattern.isEmpty()) {
            pattern = "scam";
        }

        System.out.println("\nRunning search benchmark suite. Please wait...");
        MyArrayList<AlgorithmBenchmarkResult> results = AlgorithmBenchmarkRunner.runAll(text, pattern);

        System.out.println("\n========================================= ALGORITHM BENCHMARK REPORT =========================================");
        System.out.printf("  Corpus Text Length : %d characters\n", text.length());
        System.out.printf("  Search Pattern     : \"%s\" (%d characters)\n", pattern, pattern.length());
        System.out.println("==============================================================================================================");
        System.out.printf("| %-22s | %-12s | %-16s | %-17s | %-15s | %-12s |\n",
                "Algorithm", "Matches", "Comparisons", "Execution Time", "Time Complexity", "Space Complexity");
        System.out.println("--------------------------------------------------------------------------------------------------------------");

        for (int i = 0; i < results.size(); i++) {
            AlgorithmBenchmarkResult res = results.get(i);
            String timeStr = res.getExecutionTimeNano() + " ns";
            System.out.printf("| %-22s | %-12d | %-16d | %-17s | %-15s | %-12s |\n",
                    res.getAlgorithmName(),
                    res.getMatchesFound(),
                    res.getComparisonsCount(),
                    timeStr,
                    res.getTimeComplexity(),
                    res.getSpaceComplexity()
            );
        }
        System.out.println("==============================================================================================================");
    }

    private static int getIntInput(Scanner scanner) {
        while (true) {
            try {
                String line = scanner.nextLine().trim();
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.print("Invalid integer. Please select a valid number: ");
            }
        }
    }

    private static double getDoubleInput(Scanner scanner) {
        while (true) {
            try {
                String line = scanner.nextLine().trim();
                return Double.parseDouble(line);
            } catch (NumberFormatException e) {
                System.out.print("Invalid decimal number. Enter a numeric value: $");
            }
        }
    }
}
