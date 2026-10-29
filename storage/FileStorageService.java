package storage;

import model.User;
import model.Transaction;
import util.MyArrayList;

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
