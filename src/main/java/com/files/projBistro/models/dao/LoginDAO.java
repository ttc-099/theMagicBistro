package com.files.projBistro.models.dao;

import com.files.projBistro.database.DatabaseConnection;
import com.files.projBistro.models.userModel.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

// this class handles login verification and user registration
// it talks to the database to check usernames/passwords and create new accounts
public class LoginDAO {

    // helper method to get a database connection
    private Connection getConnection() {
        return DatabaseConnection.getInstance().getConnection();
    }

    // check if a username and password match a user in the database
    // returns a User object if successful, null if not
    public User verifyLogin(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);   // put username into the query
            pstmt.setString(2, password);   // put password into the query

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // found a matching user - create and return a User object
                    return new User(
                            rs.getInt("user_id"),
                            rs.getString("username"),
                            rs.getString("role"),
                            rs.getString("phone_number")
                    );
                }
            }
        } catch (SQLException e) {
            System.out.println("Database error during login check.");
            e.printStackTrace();
        }

        return null;   // no matching user found
    }

    /**
     * registers a new user in the database with phone number and validation.
     * includes defensive programming with try/catch.
     */
    public boolean registerUser(String username, String password, String phoneNumber, String role) {
        String sql = "INSERT INTO users (username, password, phone_number, role) VALUES (?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // fill in the values for the insert statement
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, phoneNumber);
            pstmt.setString(4, role);

            int affectedRows = pstmt.executeUpdate();   // run the insert
            return affectedRows > 0;   // true if a row was inserted

        } catch (SQLException e) {
            System.err.println("❌ Registration Error: " + e.getMessage());
            // check if error is because username already exists (unique constraint)
            if (e.getMessage().contains("UNIQUE")) {
                System.err.println("Username already exists.");
            }
            return false;
        } catch (Exception e) {
            System.err.println("❌ Unexpected error during registration.");
            e.printStackTrace();
            return false;
        }
    }
}