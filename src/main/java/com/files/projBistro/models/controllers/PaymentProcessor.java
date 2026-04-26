package com.files.projBistro.models.controllers;

import com.files.projBistro.models.models.Payment;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.geometry.Insets;
import javafx.scene.layout.VBox;
import javafx.beans.value.ChangeListener;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class PaymentProcessor {

    // shows dialog asking user to choose pay at counter or credit card
    public static String showPaymentMethodDialog() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Payment Method");
        dialog.setHeaderText("Select Payment Method");

        ButtonType continueButton = new ButtonType("Continue", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(continueButton, ButtonType.CANCEL);

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setPrefWidth(300);

        ToggleGroup group = new ToggleGroup();
        RadioButton payAtCounter = new RadioButton("💵 Pay At Counter");
        RadioButton creditCard = new RadioButton("💳 Credit/Debit Card");

        payAtCounter.setToggleGroup(group);
        creditCard.setToggleGroup(group);
        payAtCounter.setSelected(true);

        VBox optionsBox = new VBox(10);
        optionsBox.getChildren().addAll(payAtCounter, creditCard);
        optionsBox.setStyle("-fx-padding: 10; -fx-border-color: #ddd; -fx-border-radius: 8; -fx-background-radius: 8;");

        content.getChildren().addAll(optionsBox);
        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(button -> {
            if (button == continueButton) {
                return creditCard.isSelected() ? "Credit/Debit Card" : "Pay At Counter";
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();
        return result.orElse(null);
    }

    // shows card entry form with validation
    public static boolean processCardPayment(double amount) {
        Dialog<Payment> paymentDialog = new Dialog<>();
        paymentDialog.setTitle("Card Payment");
        paymentDialog.setHeaderText("Enter Card Details");
        paymentDialog.setResizable(true);

        ButtonType payButton = new ButtonType("Pay RM" + String.format("%.2f", amount), ButtonBar.ButtonData.OK_DONE);
        paymentDialog.getDialogPane().getButtonTypes().addAll(payButton, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(12);
        grid.setPadding(new Insets(20));
        grid.setPrefWidth(400);

        TextField cardNumberField = new TextField();
        cardNumberField.setPromptText("1234 5678 9012 3456");

        // auto-add spaces every 4 digits
        ChangeListener<String> cardNumberListener = (obs, old, val) -> {
            String numbers = val.replaceAll("[\\s-]", "");
            if (numbers.length() > 16) {
                numbers = numbers.substring(0, 16);
            }
            StringBuilder formatted = new StringBuilder();
            for (int i = 0; i < numbers.length(); i++) {
                if (i > 0 && i % 4 == 0) {
                    formatted.append(" ");
                }
                formatted.append(numbers.charAt(i));
            }
            String newVal = formatted.toString();
            if (!newVal.equals(val)) {
                cardNumberField.setText(newVal);
                cardNumberField.positionCaret(newVal.length());
            }
        };
        cardNumberField.textProperty().addListener(cardNumberListener);

        TextField cardHolderField = new TextField();
        cardHolderField.setPromptText("JOHN DOE");

        HBox expiryCvvBox = new HBox(10);
        TextField expiryField = new TextField();
        expiryField.setPromptText("MM/YY");
        expiryField.setPrefWidth(80);

        TextField cvvField = new TextField();
        cvvField.setPromptText("CVV");
        expiryCvvBox.getChildren().addAll(expiryField, cvvField);

        // error labels
        Label cardNumberError = new Label();
        cardNumberError.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 10px;");
        Label cardHolderError = new Label();
        cardHolderError.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 10px;");
        Label expiryError = new Label();
        expiryError.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 10px;");
        Label cvvError = new Label();
        cvvError.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 10px;");

        grid.add(new Label("Card Number:"), 0, 0);
        grid.add(cardNumberField, 1, 0);
        grid.add(cardNumberError, 1, 1);
        grid.add(new Label("Card Holder:"), 0, 2);
        grid.add(cardHolderField, 1, 2);
        grid.add(cardHolderError, 1, 3);
        grid.add(new Label("Expiry / CVV:"), 0, 4);
        grid.add(expiryCvvBox, 1, 4);
        grid.add(expiryError, 1, 5);
        grid.add(cvvError, 2, 5);

        Label noteLabel = new Label("Test Card: 4111 1111 1111 1111 | Exp: 12/25 | CVV: 123");
        noteLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 10px;");
        noteLabel.setWrapText(true);
        grid.add(noteLabel, 1, 6);

        paymentDialog.getDialogPane().setContent(grid);

        Node payButtonNode = paymentDialog.getDialogPane().lookupButton(payButton);
        payButtonNode.setDisable(true);

        // real-time validation
        cardNumberField.textProperty().addListener((obs, old, val) -> {
            String cleaned = val.replaceAll("[\\s-]", "");
            if (cleaned.isEmpty()) {
                cardNumberError.setText("");
                payButtonNode.setDisable(true);
            } else if (!cleaned.matches("\\d{16}")) {
                cardNumberError.setText("Card number must be 16 digits");
                payButtonNode.setDisable(true);
            } else {
                cardNumberError.setText("");
                payButtonNode.setDisable(!isFormValid(cardNumberField, cardHolderField, expiryField, cvvField));
            }
        });

        cardHolderField.textProperty().addListener((obs, old, val) -> {
            if (val.trim().length() < 3) {
                cardHolderError.setText("Name must be at least 3 characters");
                payButtonNode.setDisable(true);
            } else {
                cardHolderError.setText("");
                payButtonNode.setDisable(!isFormValid(cardNumberField, cardHolderField, expiryField, cvvField));
            }
        });

        expiryField.textProperty().addListener((obs, old, val) -> {
            if (!val.matches("(0[1-9]|1[0-2])/\\d{2}")) {
                expiryError.setText("Use MM/YY format (e.g., 12/25)");
                payButtonNode.setDisable(true);
            } else {
                expiryError.setText("");
                payButtonNode.setDisable(!isFormValid(cardNumberField, cardHolderField, expiryField, cvvField));
            }
        });

        cvvField.textProperty().addListener((obs, old, val) -> {
            if (!val.matches("\\d{3,4}")) {
                cvvError.setText("CVV must be 3-4 digits");
                payButtonNode.setDisable(true);
            } else {
                cvvError.setText("");
                payButtonNode.setDisable(!isFormValid(cardNumberField, cardHolderField, expiryField, cvvField));
            }
        });

        paymentDialog.setResultConverter(button -> {
            if (button == payButton) {
                Payment payment = new Payment(amount);
                payment.setCardNumber(cardNumberField.getText());
                payment.setCardHolderName(cardHolderField.getText().toUpperCase());
                payment.setExpiryDate(expiryField.getText());
                payment.setCvv(cvvField.getText());
                payment.setStatus("approved");
                return payment;
            }
            return null;
        });

        Optional<Payment> result = paymentDialog.showAndWait();
        if (result.isPresent()) {
            Payment payment = result.get();
            showPaymentSuccess(payment);
            savePaymentToFile(payment);
            return true;
        }
        return false;
    }

    private static boolean isFormValid(TextField cardNum, TextField cardHolder, TextField expiry, TextField cvv) {
        return cardNum.getText().replaceAll("[\\s-]", "").matches("\\d{16}") &&
                cardHolder.getText().trim().length() >= 3 &&
                expiry.getText().matches("(0[1-9]|1[0-2])/\\d{2}") &&
                cvv.getText().matches("\\d{3,4}");
    }

    private static void showPaymentSuccess(Payment payment) {
        TextArea receiptArea = new TextArea(payment.getReceipt());
        receiptArea.setEditable(false);
        receiptArea.setPrefRowCount(15);
        receiptArea.setPrefWidth(450);

        Alert receiptAlert = new Alert(Alert.AlertType.INFORMATION);
        receiptAlert.setTitle("Payment Successful");
        receiptAlert.setHeaderText("Payment Approved!");
        receiptAlert.getDialogPane().setContent(receiptArea);
        receiptAlert.setResizable(true);
        receiptAlert.showAndWait();
    }

    // note: cvv is stored in plaintext for coursework demonstration
    // would not do this in real production
    private static void savePaymentToFile(Payment payment) {
        String filename = "payments_" + LocalDate.now() + ".csv";
        boolean fileExists = new java.io.File(filename).exists();

        try (FileWriter fw = new FileWriter(filename, true);
             PrintWriter pw = new PrintWriter(fw)) {
            if (!fileExists) {
                pw.println("Transaction ID,Date,Card Number,Card Holder,Amount,Status");
            }
            pw.printf("%s,%s,%s,%s,%.2f,%s%n",
                    payment.getTransactionId(),
                    payment.getTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                    payment.getCardNumber(),
                    payment.getCardHolderName(),
                    payment.getAmount(),
                    payment.getStatus());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}