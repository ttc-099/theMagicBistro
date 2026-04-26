package com.files.projBistro.models.dao;

import com.files.projBistro.models.database.DatabaseConnection;
import java.sql.*;

// this is a standalone test/debug program
// it connects to the database and prints out all characters and dialogue lines
// used by developers to verify data is in the database correctly
public class CheckDialogue {
    public static void main(String[] args) {
        // try-with-resources automatically closes connection and statement when done
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement()) {

            // print everything from the characters table
            System.out.println("--- CHARACTERS TABLE ---");
            ResultSet rsChars = stmt.executeQuery("SELECT * FROM characters");
            while (rsChars.next()) {
                System.out.println("ID: " + rsChars.getInt("character_id") + " | Name: " + rsChars.getString("name"));
            }

            // print everything from the character_dialogue table
            System.out.println("\n--- CHARACTER_DIALOGUE TABLE ---");
            ResultSet rsDiag = stmt.executeQuery("SELECT * FROM character_dialogue");
            while (rsDiag.next()) {
                System.out.println("ID: " + rsDiag.getInt("dialogue_id") +
                        " | CharID: " + rsDiag.getInt("character_id") +
                        " | Trigger: " + rsDiag.getString("trigger_type") +
                        " | Text: " + rsDiag.getString("dialogue_text"));
            }
        } catch (SQLException e) {
            e.printStackTrace();   // print any database errors
        }
    }
}