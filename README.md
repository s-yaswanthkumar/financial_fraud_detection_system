# Financial Fraud Detection System - Academic Review 2

A modular, zero-dependency Java system for real-time financial transaction fraud analysis, rule-based dynamic risk evaluation, and comparative string-matching algorithmic benchmarking.

---

## 📌 Project Overview

This project simulates a financial transaction processing pipeline equipped with an intelligent **Fraud Risk Engine** and **Custom String Matching Algorithms**. When transactions are initiated, the memo/payload is scanned for blacklisted fraud signatures, and account activities are analyzed against behavioral and temporal risk models.

### Key Highlights
- **Zero External Dependencies**: Pure Java SE standard libraries (`java.io`, `java.nio`, `java.time`).
- **No Built-in Collections**: Engineered using custom data structures (`MyArrayList`, `MyQueue`, custom Trie transition arrays, and primitive QuickSort for suffix sorting).
- **6 From-Scratch Pattern Matchers**: Naive, Knuth-Morris-Pratt (KMP), Z-Function, Rabin-Karp, Aho-Corasick, and Kasai Suffix Array with LCP.
- **Dynamic Risk Engine**: 0–100 scoring tier model (APPROVED, FLAGGED with OTP, BLOCKED).
- **Persistent Storage**: Java NIO-based CSV storage engine with auto-seeding for default accounts and past transactions.
- **Flat 1-Path Modular Structure**: No deeply nested folders; all packages are directly one folder away from the project root.

---

## 📂 Project Directory Structure

```
dsa-3 financial fraud detection system/
├── algorithms/                  # 6 String matching implementations
│   ├── AhoCorasickPatternMatcher.java
│   ├── AlgorithmBenchmarkRunner.java
│   ├── KasaiLcpPatternMatcher.java
│   ├── KmpPatternMatcher.java
│   ├── NaivePatternMatcher.java
│   ├── PatternMatcher.java
│   ├── RabinKarpPatternMatcher.java
│   └── ZFunctionPatternMatcher.java
├── auth/                        # Session management
│   └── AuthService.java
├── data/                        # Persistent storage (auto-seeded)
│   ├── transactions.csv
│   └── users.csv
├── engine/                      # Real-time fraud scoring engine
│   └── FraudRiskEngine.java
├── model/                       # Domain entities
│   ├── AlgorithmBenchmarkResult.java
│   ├── RiskAssessment.java
│   ├── Transaction.java
│   └── User.java
├── storage/                     # CSV file persistence & auto-seeding
│   ├── CsvFileStorageServiceImpl.java
│   └── FileStorageService.java
├── ui/                          # Interactive terminal console dashboard
│   └── ConsoleApp.java
├── util/                        # Custom generic data structures
│   ├── MyArrayList.java
│   └── MyQueue.java
├── web/                         # Web GUI frontend and HTTP API server
│   ├── index.html
│   └── WebServer.java
├── .vscode/                     # VS Code launch and settings configurations
│   ├── launch.json
│   ├── settings.json
│   └── tasks.json
├── VerificationRunner.java      # Automated system test suite
├── .gitignore                   # Excludes target/ and compiled binaries
├── pom.xml                      # Maven project descriptor
├── README.md                    # Project documentation
├── run-console.bat              # 1-Click launcher for Console App
└── run-verification.bat         # 1-Click launcher for Verification Suite
```

---

## ⚙️ Fraud Risk Evaluation Engine

The `FraudRiskEngine` calculates a real-time risk score ($S \in [0, 100]$) based on four risk factors:

| Risk Trigger | Condition | Score Weight |
|---|---|---|
| **Velocity Spike** | $\ge 3$ transactions in a 10-minute sliding window | **+25** |
| **Historical Mean Deviation** | Amount $> 3 \times$ user's historical average amount | **+20** |
| **Rapid Duplicate** | Same sender, receiver, and amount within 5 minutes | **+30** |
| **Blacklisted Signature** | Illicit phrase detected in transaction memo via pattern matcher | **+25** |

### Decision Tiers
- **`0 – 39` (APPROVED)**: Funds debited/credited immediately; transaction status saved as `APPROVED`.
- **`40 – 69` (FLAGGED / OTP)**: Transaction paused; simulated one-time password (`123456`) prompted. If correct, transaction executes; otherwise marked `FAILED`.
- **`70 – 100` (BLOCKED)**: Transaction blocked immediately; balances remain untouched; logged as `BLOCKED`.

### Blacklisted Signatures Scanned
- `"money laundering"`, `"tax evasion"`, `"shell company"`, `"phishing"`, `"scam"`
- `"mule account"`, `"offshore bank"`, `"illicit drugs"`, `"anonymous cash"`, `"wire transfer scam"`

---

## 🧠 Pattern Matching Algorithms & Complexity

All matchers implement the `PatternMatcher` interface and report exact character-level comparisons, execution duration in nanoseconds, and matches count:

| Algorithm | Preprocessing | Matching Time | Total Time Complexity | Space Complexity |
|---|---|---|---|---|
| **Naive (Brute Force)** | None | $O(N \cdot M)$ | $O(N \cdot M)$ | $O(1)$ |
| **Knuth-Morris-Pratt (KMP)** | $O(M)$ ($\pi$/LPS array) | $O(N)$ | $O(N + M)$ | $O(M)$ |
| **Z-Function** | $O(N + M)$ (Z-array) | $O(N + M)$ | $O(N + M)$ | $O(N + M)$ |
| **Rabin-Karp** | $O(M)$ (Hash precompute) | $O(N)$ avg | $O(N + M)$ avg / $O(N \cdot M)$ worst | $O(1)$ |
| **Aho-Corasick** | $O(M \cdot \Sigma)$ (Trie + BFS) | $O(N)$ | $O(N + M)$ | $O(M \cdot \Sigma)$ |
| **Kasai Suffix Array** | $O(N \log N)$ (SA) + $O(N)$ (LCP) | $O(M \log N)$ | $O(N \log N + M \log N)$ | $O(N)$ |

---

## 🚀 How to Run

### Option 1: Web GUI Dashboard (Browser Frontend)
To run the interactive web interface:
```powershell
javac -sourcepath . -d target/classes web/WebServer.java
java -cp target/classes web.WebServer
```
Then open your browser to **http://localhost:8080** *(or simply double-click `run-web.bat`)*.

### Option 2: Terminal Console Application
Open a terminal in the project root folder:
```powershell
# 1. Compile source files into target/classes:
javac -sourcepath . -d target/classes ui/ConsoleApp.java VerificationRunner.java

# 2. Run the Interactive CLI Dashboard:
java -cp target/classes ui.ConsoleApp

# 3. Or run the Automated Verification Test Suite:
java -cp target/classes VerificationRunner
```

### Option 3: 1-Click Batch Files (Windows)
- Double-click `run-web.bat` to launch the Web GUI Dashboard in your browser.
- Double-click `run-console.bat` for the CLI Console Application.
- Double-click `run-verification.bat` for the Automated Verification Suite.

### Option 3: VS Code (Run & Debug)
1. Open the project folder in VS Code.
2. Press **Ctrl + Shift + D** to open the Run & Debug view.
3. Select **"Launch ConsoleApp"** or **"Launch VerificationRunner"** from the top dropdown and press **F5**.

### Option 4: Apache Maven (Optional)
```powershell
mvn clean compile
mvn exec:java
```

---

## 🧪 Verification & Automated Test Results

The test suite ([`VerificationRunner.java`](file:///VerificationRunner.java)) validates:
1. Pattern matching correctness across all 6 matchers on complex text.
2. Character comparison tracking.
3. Risk calculation accuracy across all 5 test scenarios (Normal, Deviation, Blacklist, Duplicate, Multi-Factor Combo).

```
==========================================================
            RUNNING SYSTEM VERIFICATION TESTS             
==========================================================

[TEST] Verifying Pattern Matching Algorithms...
  - Naive (Brute Force)      : Matches Found = 2 (Expected = 2) | Comparisons = 33 | Status = PASS
  - Knuth-Morris-Pratt       : Matches Found = 2 (Expected = 2) | Comparisons = 33 | Status = PASS
  - Z-Function               : Matches Found = 2 (Expected = 2) | Comparisons = 35 | Status = PASS
  - Rabin-Karp               : Matches Found = 2 (Expected = 2) | Comparisons = 8 | Status = PASS
  - Aho-Corasick             : Matches Found = 2 (Expected = 2) | Comparisons = 34 | Status = PASS
  - Kasai Suffix Array       : Matches Found = 2 (Expected = 2) | Comparisons = 288 | Status = PASS

[TEST] Verifying Fraud Risk Assessment Engine...
  - Test Case 1 (Normal Tx)     : Score = 0.0, Tier = APPROVED | Expected = 0.0, APPROVED | Status = PASS
  - Test Case 2 (Deviation Tx)  : Score = 20.0 | Expected = 20.0 | Status = PASS
  - Test Case 3 (Blacklist Memo): Score = 25.0 | Expected = 25.0 | Status = PASS
  - Test Case 4 (Duplicate Tx)  : Score = 30.0 | Expected = 30.0 | Status = PASS
  - Test Case 5 (Combo Trigger) : Score = 70.0, Tier = BLOCKED | Expected = 70.0, BLOCKED | Status = PASS

==========================================================
        STATUS: ALL SYSTEM TESTS PASSED SUCCESSFULLY!    
==========================================================
```


