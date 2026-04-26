package com.files.projBistro.models.dao;

import org.junit.jupiter.api.*;
import java.sql.*;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class DialogueDAOTest {

    private Connection connection;
    private DialogueDAO dialogueDAO;

    @BeforeEach
    void setUp() throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite::memory:");
        dialogueDAO = new TestDialogueDAO(connection);
        createTestSchema();
        insertTestData();
    }

    @AfterEach
    void tearDown() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    private void createTestSchema() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE TABLE characters (character_id INTEGER PRIMARY KEY, name TEXT)");
            stmt.execute("INSERT INTO characters VALUES (1, 'Chloe'), (2, 'Mimi'), (3, 'Metsu'), (4, 'Laniard')");

            stmt.execute("CREATE TABLE character_dialogue (" +
                    "dialogue_id INTEGER PRIMARY KEY, character_id INTEGER, " +
                    "trigger_type TEXT, dialogue_text TEXT)");
        }
    }

    private void insertTestData() throws SQLException {
        try (PreparedStatement pstmt = connection.prepareStatement(
                "INSERT INTO character_dialogue (dialogue_id, character_id, trigger_type, dialogue_text) VALUES (?,?,?,?)")) {
            pstmt.setInt(1, 1);
            pstmt.setInt(2, 1);
            pstmt.setString(3, "WELCOME");
            pstmt.setString(4, "Welcome to my cafe!");
            pstmt.executeUpdate();

            pstmt.setInt(1, 2);
            pstmt.setInt(2, 1);
            pstmt.setString(3, "WELCOME");
            pstmt.setString(4, "Hello there!");
            pstmt.executeUpdate();

            pstmt.setInt(1, 3);
            pstmt.setInt(2, 2);
            pstmt.setString(3, "ORDER_COMPLETE");
            pstmt.setString(4, "Enjoy your meal!");
            pstmt.executeUpdate();
        }
    }

    @Test
    void testGetRandomDialogue_ReturnsMatchingDialogue() {
        String dialogue = dialogueDAO.getRandomDialogue(1, "WELCOME");
        assertNotNull(dialogue);
        assertTrue(dialogue.equals("Welcome to my cafe!") || dialogue.equals("Hello there!"));
    }

    @Test
    void testGetRandomDialogue_NoMatch_ReturnsDefault() {
        String dialogue = dialogueDAO.getRandomDialogue(99, "UNKNOWN");
        assertEquals("Unit standing by...", dialogue);
    }

    @Test
    void testGetDialoguesByCharacter_ChloeReturnsTwo() {
        List<DialogueDAO.DialogueEntry> dialogues = dialogueDAO.getDialoguesByCharacter(1);
        assertEquals(2, dialogues.size());
    }

    @Test
    void testGetDialoguesByCharacter_MimiReturnsOne() {
        List<DialogueDAO.DialogueEntry> dialogues = dialogueDAO.getDialoguesByCharacter(2);
        assertEquals(1, dialogues.size());
    }

    @Test
    void testGetDialoguesByCharacter_InvalidId_ReturnsEmpty() {
        List<DialogueDAO.DialogueEntry> dialogues = dialogueDAO.getDialoguesByCharacter(99);
        assertTrue(dialogues.isEmpty());
    }

    private static class TestDialogueDAO extends DialogueDAO {
        private final Connection testConnection;

        TestDialogueDAO(Connection conn) {
            this.testConnection = conn;
        }

        @Override
        protected Connection getConnection() {
            return testConnection;
        }
    }
}