package com.files.projBistro.models;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class Order {
    private int orderId;
    private String customerName;
    private String customerPhone;  // new field for phone number
    private List<FoodItem> items;
    private double totalPrice;
    private Timestamp orderDate;
    private String status;

    public Order(int orderId, String customerName) {
        this.orderId = orderId;
        this.customerName = customerName;
        this.customerPhone = "";
        this.items = new ArrayList<>();
        this.totalPrice = 0.0;
        this.status = "pending";
    }

    public Order(String customerName) {
        this.customerName = customerName;
        this.customerPhone = "";
        this.items = new ArrayList<>();
        this.totalPrice = 0.0;
        this.status = "pending";
    }

    public void calculateTotal() {
        double sum = 0;
        for (FoodItem item : items) {
            sum += item.getPrice();
        }
        this.totalPrice = sum;
    }

    public void addItem(FoodItem item) {
        items.add(item);
        calculateTotal();
    }

    public void removeItem(FoodItem item) {
        items.remove(item);
        calculateTotal();
    }

    // Getters and Setters
    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }

    public List<FoodItem> getItems() { return items; }

    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }

    public Timestamp getOrderDate() { return orderDate; }
    public void setOrderDate(Timestamp orderDate) { this.orderDate = orderDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}