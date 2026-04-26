package com.files.projBistro.models.controllers.admin;

import com.files.projBistro.models.dao.AdminDAO;
import com.files.projBistro.models.dao.DialogueDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class AdminDialogueController {

    // ui elements from fxml
    private ComboBox<String> dialogueCharBox;  // select which character
    private TableView<DialogueDAO.DialogueEntry> dialogueTable;  // shows all dialogue lines
    private TableColumn<DialogueDAO.DialogueEntry, Integer> colDialogueId;
    private TableColumn<DialogueDAO.DialogueEntry, String> colTrigger;
    private TableColumn<DialogueDAO.DialogueEntry, String> colText;
    private ComboBox<String> triggerInputBox;  // for adding new dialogue
    private TextArea dialogueInputArea;  // text for new dialogue
    private ComboBox<String> editTriggerBox;  // for editing existing dialogue
    private TextArea editDialogueArea;  // text for editing

    private AdminDAO adminDAO;
    private DialogueDAO dialogueDAO;
    private BooleanSupplier isAuthorized;  // checks if admin pin was entered
    private Consumer<String> showStatus;  // shows temporary status messages

    public void init(AdminDAO adminDAO, DialogueDAO dialogueDAO, Label statusLabel,
                     Consumer<String> showStatus, BooleanSupplier isAuthorized) {
        this.adminDAO = adminDAO;
        this.dialogueDAO = dialogueDAO;
        this.showStatus = showStatus;
        this.isAuthorized = isAuthorized;
    }

    // connects all the ui elements after fxml loads
    public void setUIElements(ComboBox<String> dialogueCharBox,
                              TableView<DialogueDAO.DialogueEntry> dialogueTable,
                              TableColumn<DialogueDAO.DialogueEntry, Integer> colDialogueId,
                              TableColumn<DialogueDAO.DialogueEntry, String> colTrigger,
                              TableColumn<DialogueDAO.DialogueEntry, String> colText,
                              ComboBox<String> triggerInputBox,
                              TextArea dialogueInputArea,
                              ComboBox<String> editTriggerBox,
                              TextArea editDialogueArea) {
        this.dialogueCharBox = dialogueCharBox;
        this.dialogueTable = dialogueTable;
        this.colDialogueId = colDialogueId;
        this.colTrigger = colTrigger;
        this.colText = colText;
        this.triggerInputBox = triggerInputBox;
        this.dialogueInputArea = dialogueInputArea;
        this.editTriggerBox = editTriggerBox;
        this.editDialogueArea = editDialogueArea;

        setupTableColumns();
        setupCharacterSelectionListener();
        setupTableSelectionListener();
        setupDropdowns();
    }

    // tell table columns which data to show from DialogueEntry objects
    private void setupTableColumns() {
        colDialogueId.setCellValueFactory(cellData -> cellData.getValue().idProperty().asObject());
        colTrigger.setCellValueFactory(cellData -> cellData.getValue().triggerTypeProperty());
        colText.setCellValueFactory(cellData -> cellData.getValue().textProperty());
    }

    // when admin selects a different character, reload the table
    private void setupCharacterSelectionListener() {
        dialogueCharBox.getSelectionModel().selectedItemProperty().addListener((obs, old, character) -> {
            if (character != null) {
                refreshTable();
                clearDialogueSelection();
                clearAddDialogueForm();
            }
        });
    }

    // when admin clicks a row in the table, load it into edit form
    private void setupTableSelectionListener() {
        dialogueTable.getSelectionModel().selectedItemProperty().addListener((obs, old, newItem) -> {
            if (newItem != null) {
                editTriggerBox.setValue(newItem.getTriggerType());
                editDialogueArea.setText(newItem.getText());
                clearAddDialogueForm();
            }
        });
    }

    // fill the dropdown boxes with options
    private void setupDropdowns() {
        ObservableList<String> characters = FXCollections.observableArrayList("Chloe", "Mimi", "Metsu", "Laniard");
        dialogueCharBox.setItems(characters);
        dialogueCharBox.getSelectionModel().selectFirst();

        ObservableList<String> triggerOptions = FXCollections.observableArrayList(
                "ITEM_SELECTED", "ORDER_COMPLETE", "LOGIN_GREET", "LOW_STOCK", "WELCOME"
        );
        triggerInputBox.setItems(triggerOptions);
        editTriggerBox.setItems(triggerOptions);
    }

    // add a new dialogue line to database
    public void handleAddDialogue() {
        if (!isAuthorized.getAsBoolean()) return;
        int charId = dialogueCharBox.getSelectionModel().getSelectedIndex() + 1;
        String trigger = triggerInputBox.getValue();
        String text = dialogueInputArea.getText();
        if (charId < 1 || trigger == null || trigger.isEmpty() || text == null || text.isEmpty()) {
            showStatus.accept("Please fill in all dialogue fields.");
            return;
        }
        if (adminDAO.addDialogue(charId, trigger, text)) {
            clearAddDialogueForm();
            refreshTable();
            showStatus.accept("Dialogue added successfully!");
        } else {
            showStatus.accept("Failed to add dialogue.");
        }
    }

    // update existing dialogue line
    public void handleUpdateDialogue() {
        if (!isAuthorized.getAsBoolean()) return;
        DialogueDAO.DialogueEntry selected = dialogueTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showStatus.accept("Please select a dialogue line from the table first.");
            return;
        }
        String trigger = editTriggerBox.getValue();
        String text = editDialogueArea.getText();
        if (trigger == null || trigger.isEmpty() || text == null || text.isEmpty()) {
            showStatus.accept("Please fill in both trigger type and dialogue text.");
            return;
        }
        if (adminDAO.updateDialogue(selected.getId(), trigger, text)) {
            refreshTable();
            clearDialogueSelection();
            showStatus.accept("Dialogue updated successfully!");
        } else {
            showStatus.accept("Failed to update dialogue.");
        }
    }

    // permanently delete a dialogue line
    public void handleDeleteDialogue() {
        if (!isAuthorized.getAsBoolean()) return;
        DialogueDAO.DialogueEntry selected = dialogueTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showStatus.accept("Please select a dialogue line from the table first.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Dialogue");
        confirm.setHeaderText("Delete this dialogue line?");
        confirm.setContentText("This action cannot be undone.");
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            if (adminDAO.deleteDialogue(selected.getId())) {
                refreshTable();
                clearDialogueSelection();
                showStatus.accept("Dialogue deleted successfully!");
            } else {
                showStatus.accept("Failed to delete dialogue.");
            }
        }
    }

    // clear the edit form and table selection
    public void clearDialogueSelection() {
        dialogueTable.getSelectionModel().clearSelection();
        editTriggerBox.getSelectionModel().clearSelection();
        editDialogueArea.clear();
        showStatus.accept("Selection cleared.");
    }

    // clear the add new dialogue form
    private void clearAddDialogueForm() {
        triggerInputBox.getSelectionModel().clearSelection();
        dialogueInputArea.clear();
    }

    // reload the dialogue table for current character
    public void refreshTable() {
        int charId = dialogueCharBox.getSelectionModel().getSelectedIndex() + 1;
        if (charId > 0 && charId <= 4) {
            List<DialogueDAO.DialogueEntry> dialogues = dialogueDAO.getDialoguesByCharacter(charId);
            dialogueTable.setItems(FXCollections.observableArrayList(dialogues));
            dialogueTable.refresh();
        } else {
            dialogueTable.setItems(FXCollections.observableArrayList());
        }
    }
}