package com.files.projBistro.models.dao;

import com.files.projBistro.models.database.DatabaseConnection;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// this class handles all database operations related to character dialogue
// it can fetch random dialogue lines or all lines for a specific character
public class DialogueDAO {

    // helper method to get a database connection
    protected Connection getConnection() {
        return DatabaseConnection.getInstance().getConnection();
    }

    // get a random dialogue line for a specific character and trigger type
    // example: character "Chloe" with trigger "WELCOME" might say "hello there!"
    public String getRandomDialogue(int characterId, String triggerType) {
        List<String> options = new ArrayList<>();   // store all matching dialogue lines
        String sql = "SELECT dialogue_text FROM character_dialogue WHERE character_id = ? AND trigger_type = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, characterId);      // which character
            pstmt.setString(2, triggerType);   // what event happened
            ResultSet rs = pstmt.executeQuery();

            // collect all matching dialogue lines into the list
            while (rs.next()) {
                options.add(rs.getString("dialogue_text"));
            }
        } catch (SQLException e) {
            System.out.println("❌ Error fetching dialogue: " + e.getMessage());
        }

        // if we found any dialogue lines, pick one at random
        if (!options.isEmpty()) {
            return options.get(new Random().nextInt(options.size()));
        }
        // default message if no dialogue found
        return "Unit standing by...";
    }

    // get all dialogue lines for a specific character (for admin editing)
    public List<DialogueEntry> getDialoguesByCharacter(int characterId) {
        System.out.println("[SYSTEM_LOG] DialogueDAO: Fetching dialogues for characterId: " + characterId);
        List<DialogueEntry> dialogues = new ArrayList<>();
        String sql = "SELECT dialogue_id, trigger_type, dialogue_text FROM character_dialogue WHERE character_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, characterId);
            ResultSet rs = pstmt.executeQuery();

            // convert each database row into a DialogueEntry object
            while (rs.next()) {
                DialogueEntry entry = new DialogueEntry(
                        rs.getInt("dialogue_id"),
                        rs.getString("trigger_type"),
                        rs.getString("dialogue_text")
                );
                dialogues.add(entry);
            }
            System.out.println("[SYSTEM_LOG] DialogueDAO: Found " + dialogues.size() + " records in DB.");
        } catch (SQLException e) {
            System.out.println("❌ Error fetching dialogues: " + e.getMessage());
            e.printStackTrace();
        }
        return dialogues;
    }

    // ========== inner class ==========
    // this class represents a single line of dialogue
    // it uses javafx properties so it can be displayed in tables and update automatically
    public static class DialogueEntry {
        // properties allow the ui to listen for changes
        private final IntegerProperty id = new SimpleIntegerProperty();
        private final StringProperty triggerType = new SimpleStringProperty();
        private final StringProperty text = new SimpleStringProperty();

        // constructor creates a dialogue entry with given values
        public DialogueEntry(int id, String triggerType, String text) {
            this.id.set(id);
            this.triggerType.set(triggerType);
            this.text.set(text);
        }

        // regular getters for normal java code
        public int getId() { return id.get(); }
        public String getTriggerType() { return triggerType.get(); }
        public String getText() { return text.get(); }

        // property getters for javafx table binding
        public IntegerProperty idProperty() { return id; }
        public StringProperty triggerTypeProperty() { return triggerType; }
        public StringProperty textProperty() { return text; }
    }
}