package com.fraud.detection.storage;

import com.fraud.detection.model.User;
import com.fraud.detection.model.Transaction;
import com.fraud.detection.util.MyArrayList;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CsvFileStorageServiceImpl implements FileStorageService {
    private final Path usersPath;
    private final Path transactionsPath;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public CsvFileStorageServiceImpl() {
        this("data/users.csv", "data/transactions.csv");
    }

    public CsvFileStorageServiceImpl(String usersFilePath, String transactionsFilePath) {
        this.usersPath = Paths.get(usersFilePath);
        this.transactionsPath = Paths.get(transactionsFilePath);
        try {
            if (usersPath.getParent() != null) {
                Files.createDirectories(usersPath.getParent());
            }
            if (transactionsPath.getParent() != null) {
                Files.createDirectories(transactionsPath.getParent());
            }
            if (!Files.exists(usersPath)) {
                seedDefaultUsers();
            }
            if (!Files.exists(transactionsPath)) {
                seedDefaultTransactions();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public synchronized MyArrayList<User> loadUsers() {
        MyArrayList<User> users = new MyArrayList<>();
        if (!Files.exists(usersPath)) {
            return users;
        }
        try (BufferedReader reader = Files.newBufferedReader(usersPath)) {
            String line;
            reader.readLine();
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                MyArrayList<String> fields = parseCsvLine(line);
                if (fields.size() >= 5) {
                    User user = new User();
                    user.setUserId(fields.get(0));
                    user.setUsername(fields.get(1));
                    user.setPassword(fields.get(2));
                    user.setBalance(Double.parseDouble(fields.get(3)));
                    user.setCreatedAt(LocalDateTime.parse(fields.get(4), DATE_TIME_FORMATTER));
                    users.add(user);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return users;
    }

    @Override
    public synchronized void saveUsers(MyArrayList<User> users) {
        try (BufferedWriter writer = Files.newBufferedWriter(usersPath)) {
            writer.write("userId,username,password,balance,createdAt");
            writer.newLine();
            for (int i = 0; i < users.size(); i++) {
                User u = users.get(i);
                writer.write(String.join(",",
                        escapeCsv(u.getUserId()),
                        escapeCsv(u.getUsername()),
                        escapeCsv(u.getPassword()),
                        String.valueOf(u.getBalance()),
                        u.getCreatedAt().format(DATE_TIME_FORMATTER)
                ));
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized MyArrayList<Transaction> loadTransactions() {
        MyArrayList<Transaction> transactions = new MyArrayList<>();
        if (!Files.exists(transactionsPath)) {
            return transactions;
        }
        try (BufferedReader reader = Files.newBufferedReader(transactionsPath)) {
            String line;
            reader.readLine();
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                MyArrayList<String> fields = parseCsvLine(line);
                if (fields.size() >= 8) {
                    Transaction tx = new Transaction();
                    tx.setTxId(fields.get(0));
                    tx.setSenderId(fields.get(1));
                    tx.setReceiverId(fields.get(2));
                    tx.setAmount(Double.parseDouble(fields.get(3)));
                    tx.setMemo(fields.get(4));
                    tx.setTimestamp(LocalDateTime.parse(fields.get(5), DATE_TIME_FORMATTER));
                    tx.setRiskScore(Double.parseDouble(fields.get(6)));
                    tx.setStatus(fields.get(7));
                    transactions.add(tx);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return transactions;
    }

    @Override
    public synchronized void saveTransactions(MyArrayList<Transaction> transactions) {
        try (BufferedWriter writer = Files.newBufferedWriter(transactionsPath)) {
            writer.write("txId,senderId,receiverId,amount,memo,timestamp,riskScore,status");
            writer.newLine();
            for (int i = 0; i < transactions.size(); i++) {
                Transaction tx = transactions.get(i);
                writer.write(String.join(",",
                        escapeCsv(tx.getTxId()),
                        escapeCsv(tx.getSenderId()),
                        escapeCsv(tx.getReceiverId()),
                        String.valueOf(tx.getAmount()),
                        escapeCsv(tx.getMemo()),
                        tx.getTimestamp().format(DATE_TIME_FORMATTER),
                        String.valueOf(tx.getRiskScore()),
                        escapeCsv(tx.getStatus())
                ));
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized void addUser(User user) {
        MyArrayList<User> users = loadUsers();
        users.add(user);
        saveUsers(users);
    }

    @Override
    public synchronized void addTransaction(Transaction transaction) {
        MyArrayList<Transaction> txs = loadTransactions();
        txs.add(transaction);
        saveTransactions(txs);
    }

    @Override
    public synchronized User findUserById(String userId) {
        MyArrayList<User> users = loadUsers();
        for (int i = 0; i < users.size(); i++) {
            User u = users.get(i);
            if (u.getUserId().equalsIgnoreCase(userId)) {
                return u;
            }
        }
        return null;
    }

    @Override
    public synchronized User findUserByUsername(String username) {
        MyArrayList<User> users = loadUsers();
        for (int i = 0; i < users.size(); i++) {
            User u = users.get(i);
            if (u.getUsername().equalsIgnoreCase(username)) {
                return u;
            }
        }
        return null;
    }

    private void seedDefaultUsers() {
        MyArrayList<User> users = new MyArrayList<>();
        LocalDateTime baseTime = LocalDateTime.now().minusDays(5);
        users.add(new User("U101", "alice", "pass123", 5000.0, baseTime));
        users.add(new User("U102", "bob", "pass123", 5000.0, baseTime.plusHours(1)));
        users.add(new User("U103", "charlie", "pass123", 5000.0, baseTime.plusHours(2)));
        users.add(new User("U104", "david", "pass123", 5000.0, baseTime.plusHours(3)));
        users.add(new User("U105", "eve", "pass123", 5000.0, baseTime.plusHours(4)));
        saveUsers(users);
    }

    private void seedDefaultTransactions() {
        MyArrayList<Transaction> txs = new MyArrayList<>();
        LocalDateTime baseTime = LocalDateTime.now().minusDays(3);
        
        txs.add(new Transaction("TX1001", "U101", "U102", 100.0, "Lunch split", baseTime, 5.0, "APPROVED"));
        txs.add(new Transaction("TX1002", "U101", "U102", 150.0, "Grocery split", baseTime.plusDays(1), 10.0, "APPROVED"));
        txs.add(new Transaction("TX1003", "U101", "U102", 50.0, "Rent utility", baseTime.plusDays(2), 2.0, "APPROVED"));
        txs.add(new Transaction("TX1004", "U102", "U103", 200.0, "Gadget buy", baseTime.plusHours(12), 12.0, "APPROVED"));
        txs.add(new Transaction("TX1005", "U102", "U103", 300.0, "Phone upgrade", baseTime.plusDays(1).plusHours(6), 18.0, "APPROVED"));
        
        saveTransactions(txs);
    }

    private MyArrayList<String> parseCsvLine(String line) {
        MyArrayList<String> result = new MyArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    sb.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',') {
                if (inQuotes) {
                    sb.append(c);
                } else {
                    result.add(sb.toString());
                    sb.setLength(0);
                }
            } else {
                sb.append(c);
            }
        }
        result.add(sb.toString());
        return result;
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
