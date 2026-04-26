package com.files.projBistro.models.models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Payment {
    private String cardNumber;
    private String cardHolderName;
    private String expiryDate;
    private String cvv;
    private double amount;
    private String status;
    private String transactionId;
    private LocalDateTime timestamp;

    public Payment(double amount) {
        this.amount = amount;
        this.status = "pending";
        this.transactionId = generateTransactionId();
        this.timestamp = LocalDateTime.now();
    }

    private String generateTransactionId() {
        return "TXN" + System.currentTimeMillis();
    }

    // Getters and setters
    public String getCardNumber() { return cardNumber; }
    public void setCardNumber(String cardNumber) { this.cardNumber = maskCardNumber(cardNumber); }

    public String getCardHolderName() { return cardHolderName; }
    public void setCardHolderName(String cardHolderName) { this.cardHolderName = cardHolderName; }

    public String getExpiryDate() { return expiryDate; }
    public void setExpiryDate(String expiryDate) { this.expiryDate = expiryDate; }

    public String getCvv() { return cvv; }
    public void setCvv(String cvv) { this.cvv = cvv; }

    public double getAmount() { return amount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getTransactionId() { return transactionId; }
    public LocalDateTime getTimestamp() { return timestamp; }

    private String maskCardNumber(String number) {
        if (number == null || number.length() < 4) return "****";
        String last4 = number.substring(number.length() - 4);
        return "**** **** **** " + last4;
    }

    public String getReceipt() {
        return String.format("""
            ==================================================
                        PAYMENT RECEIPT
            ==================================================
            Transaction ID: %s
            Date & Time: %s
            Card: %s
            Card Holder: %s
            Amount: RM%.2f
            Status: %s
            ==================================================
            Thank you for your payment!
            ==================================================
            """,
                transactionId,
                timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                cardNumber,
                cardHolderName,
                amount,
                status.toUpperCase()
        );
    }
}