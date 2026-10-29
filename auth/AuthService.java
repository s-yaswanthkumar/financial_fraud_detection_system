package auth;

import model.User;
import storage.FileStorageService;
import util.MyArrayList;

import java.time.LocalDateTime;

public class AuthService {
    private final FileStorageService storageService;
    private User currentUser;

    public AuthService(FileStorageService storageService) {
        this.storageService = storageService;
    }

    public synchronized boolean register(String username, String password, double initialBalance) {
        if (storageService.findUserByUsername(username) != null) {
            return false;
        }

        MyArrayList<User> users = storageService.loadUsers();
        int maxNum = 100;
        for (int i = 0; i < users.size(); i++) {
            User u = users.get(i);
            try {
                String numericPart = u.getUserId().replaceAll("^U", "");
                int num = Integer.parseInt(numericPart);
                if (num > maxNum) {
                    maxNum = num;
                }
            } catch (NumberFormatException e) {
            }
        }
        String newUserId = "U" + (maxNum + 1);

        User newUser = new User(newUserId, username, password, initialBalance, LocalDateTime.now());
        storageService.addUser(newUser);
        return true;
    }

    public synchronized User login(String username, String password) {
        User user = storageService.findUserByUsername(username);
        if (user != null && user.getPassword().equals(password)) {
            currentUser = user;
            return user;
        }
        return null;
    }

    public synchronized void logout() {
        currentUser = null;
    }

    public synchronized User getCurrentUser() {
        return currentUser;
    }

    public synchronized void refreshCurrentUser() {
        if (currentUser != null) {
            currentUser = storageService.findUserById(currentUser.getUserId());
        }
    }
}
