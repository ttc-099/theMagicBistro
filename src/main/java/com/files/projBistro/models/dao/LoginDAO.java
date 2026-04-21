package com.files.projBistro.models.dao;

import com.files.projBistro.models.database.DatabaseConnection;
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

    // Verify if a PIN belongs to ANY admin
    public boolean verifyAdminPin(String pin) {
        String sql = "SELECT user_id FROM users WHERE role = 'Admin' AND admin_pin = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, pin);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Get admin details by PIN (returns User object)
    public User getAdminByPin(String pin) {
        String sql = "SELECT user_id, username, role, phone_number FROM users WHERE role = 'Admin' AND admin_pin = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, pin);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new User(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("role"),
                        rs.getString("phone_number")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Verify admin by username + password + PIN (most secure)
    public User verifyFullAdminLogin(String username, String password, String pin) {
        String sql = "SELECT user_id, username, role, phone_number FROM users WHERE role = 'Admin' AND username = ? AND password = ? AND admin_pin = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, pin);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new User(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("role"),
                        rs.getString("phone_number")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Verify PIN for a SPECIFIC admin by username
    public User verifyAdminByUsernameAndPin(String username, String pin) {
        String sql = "SELECT user_id, username, role, phone_number FROM users WHERE role = 'Admin' AND username = ? AND admin_pin = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, pin);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new User(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("role"),
                        rs.getString("phone_number")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


}