package com.files.projBistro.models.models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Feedback {
    private int orderId;
    private int rating;        // 1-5 stars
    private String comment;
    private String foodQuality;
    private String deliverySpeed;
    private LocalDateTime timestamp;

    public Feedback(int orderId) {
        this.orderId = orderId;
        this.timestamp = LocalDateTime.now();
    }

    // Getters and setters
    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public String getFoodQuality() { return foodQuality; }
    public void setFoodQuality(String foodQuality) { this.foodQuality = foodQuality; }

    public String getDeliverySpeed() { return deliverySpeed; }
    public void setDeliverySpeed(String deliverySpeed) { this.deliverySpeed = deliverySpeed; }

    public LocalDateTime getTimestamp() { return timestamp; }

    public String getRatingStars() {
        return "⭐".repeat(rating);
    }

    public String toCSV() {
        return String.format("%d,%d,%s,%s,%s,%s",
                orderId,
                rating,
                comment != null ? comment.replace(",", ";") : "",
                foodQuality != null ? foodQuality : "",
                deliverySpeed != null ? deliverySpeed : "",
                timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        );
    }
}