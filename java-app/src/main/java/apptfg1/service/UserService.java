package apptfg1.service;

import apptfg1.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UserService {
    private static final String USERS_FILE = "users.json";
    private List<User> users;
    private ObjectMapper objectMapper;

    public UserService() {
        this.objectMapper = new ObjectMapper();
        this.users = loadUsers();
    }

    private List<User> loadUsers() {
        File file = new File(USERS_FILE);
        if (file.exists()) {
            try {
                User[] usersArray = objectMapper.readValue(file, User[].class);
                return new ArrayList<>(Arrays.asList(usersArray));
            } catch (IOException e) {
                System.err.println("Error cargando usuarios: " + e.getMessage());
            }
        }
        return new ArrayList<>();
    }

    private void saveUsers() {
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(USERS_FILE), users);
        } catch (IOException e) {
            System.err.println("Error guardando usuarios: " + e.getMessage());
        }
    }

    public boolean registerUser(String username, String password, String name, String phone, String email) {
        if (username == null || username.trim().isEmpty() || 
            password == null || password.trim().isEmpty() ||
            name == null || name.trim().isEmpty()) {
            return false;
        }

        if (userExists(username)) {
            return false;
        }

        User newUser = new User(username, password, name, phone, email);
        users.add(newUser);
        saveUsers();
        return true;
    }

    public User loginUser(String username, String password) {
        for (User user : users) {
            if (user.getUsername().equals(username) && user.getPassword().equals(password)) {
                return user;
            }
        }
        return null;
    }

    public boolean userExists(String username) {
        return users.stream().anyMatch(u -> u.getUsername().equals(username));
    }

    public User getUserByUsername(String username) {
        return users.stream()
                .filter(u -> u.getUsername().equals(username))
                .findFirst()
                .orElse(null);
    }
}

