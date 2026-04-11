package com.files.projBistro.models.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DialogueDAO {
    // 1. connection string to our tactical bistro database
    private final String URL = "jdbc:sqlite:bistroTrue.db";

    // 2. fetch a random line of dialogue based on character and trigger
    // this makes the "Good Choice!" feel less repetitive
    public String getRandomDialogue(int characterId, String triggerType) {
        List<String> options = new ArrayList<>();
        String sql = "SELECT dialogue_text FROM character_dialogue WHERE character_id = ? AND trigger_type = ?";

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, characterId);
            pstmt.setString(2, triggerType);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                options.add(rs.getString("dialogue_text"));
            }
        } catch (SQLException e) {
            System.out.println("❌ Error fetching dialogue: " + e.getMessage());
        }

        // 3. pick a random line if multiple exist, else return a fallback
        if (!options.isEmpty()) {
            return options.get(new Random().nextInt(options.size()));
        }
        return "Unit standing by..."; // tactical fallback string
    }

    public List<DialogueEntry> getDialoguesByCharacter(int characterId) {
        List<DialogueEntry> dialogues = new ArrayList<>();
        String sql = "SELECT dialogue_id, trigger_type, dialogue_text FROM character_dialogue WHERE character_id = ?";

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, characterId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                DialogueEntry entry = new DialogueEntry(
                        rs.getInt("dialogue_id"),
                        rs.getString("trigger_type"),
                        rs.getString("dialogue_text")
                );
                dialogues.add(entry);
            }
        } catch (SQLException e) {
            System.out.println("❌ Error fetching dialogues: " + e.getMessage());
        }
        return dialogues;
    }

    // Inner class for dialogue entries (create this in its own file or as inner class)
    public static class DialogueEntry {
        private int id;
        private String triggerType;
        private String text;

        public DialogueEntry(int id, String triggerType, String text) {
            this.id = id;
            this.triggerType = triggerType;
            this.text = text;
        }

        public int getId() { return id; }
        public String getTriggerType() { return triggerType; }
        public String getText() { return text; }
    }
}