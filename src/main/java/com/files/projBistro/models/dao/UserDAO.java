package com.files.projBistro.models.dao;
import com.files.projBistro.models.userModel.User;
import com.files.projBistro.models.userModel.Role;

public class UserDAO {
    public User verifyLogin(String username, String password) {
        // TEMP TEST LOGIC
        if (username.equals("admin") && password.equals("123")) {
            return new User("admin", "password123", Role.ADMIN);
        }
        return null;
    }
}
