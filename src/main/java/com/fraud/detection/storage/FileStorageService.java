package com.fraud.detection.storage;

import com.fraud.detection.model.User;
import com.fraud.detection.model.Transaction;
import com.fraud.detection.util.MyArrayList;

public interface FileStorageService {
    MyArrayList<User> loadUsers();
    void saveUsers(MyArrayList<User> users);
    MyArrayList<Transaction> loadTransactions();
    void saveTransactions(MyArrayList<Transaction> transactions);
    
    void addUser(User user);
    void addTransaction(Transaction transaction);
    User findUserById(String userId);
    User findUserByUsername(String username);
}
