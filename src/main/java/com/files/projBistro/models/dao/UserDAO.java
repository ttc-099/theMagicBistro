package com.files.projBistro.models.dao;

import com.files.projBistro.models.userModel.User;

// this class is a simple wrapper/redirector for user-related database operations
// it doesn't do the work itself - it just calls the logindao methods
// this is for convenience and to keep code organized
public class UserDAO {

    // verify a user's login credentials
    // simply passes the work to logindao
    public User verifyLogin(String username, String password) {
        // use the existing logindao logic for consistency
        return new LoginDAO().verifyLogin(username, password);
    }

    // register a new user
    // simply passes the work to logindao
    public boolean registerUser(String username, String password, String phoneNumber, String role) {
        return new LoginDAO().registerUser(username, password, phoneNumber, role);
    }
}