package com.files.projBistro.models.dao;

import com.files.projBistro.models.userModel.User;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginDAO {
    // The link to our tactical database file
    private static final String URL = "jdbc:sqlite:bistroTrue.db";

    public User verifyLogin(String username, String password) {
        // The command to find a user that matches the typed name and pass
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";

        // Try to open a connection to the database
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Fill in the blanks (?) with the user's input
            pstmt.setString(1, username);
            pstmt.setString(2, password);

            // Run the search and look at the results
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // If we found a match, create a new User object using
                    // the ID, Name, and Role (Admin/Customer) from the table
                    return new User(
                            rs.getInt("user_id"),
                            rs.getString("username"),
                            rs.getString("role")
                    );
                }
            }
        } catch (SQLException e) {
            // If something goes wrong (like a missing file), print the error
            System.out.println("Database error during login check.");
            e.printStackTrace();
        }

        // If no match was found, return nothing
        return null;
    }
}