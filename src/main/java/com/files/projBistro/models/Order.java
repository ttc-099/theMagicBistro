package com.files.projBistro.models;

import java.util.ArrayList;
import java.util.List;

public class Order {
    // store list of items purchased... and total price

    // expected fields
    private int orderId;
    private String customerName;
    private List<FoodItem> items; // your cart
    private double totalPrice;

    // constructor
    // (a normal one is used here bc there's not
    // that many fields here)
    public Order(int orderId, String customerName) {
        this.orderId = orderId;
        this.customerName = customerName;
        this.items = new ArrayList<>(); // Initialize the empty tray
        this.totalPrice = 0.0;
    }

    // 3a. functional methods
    // most methods here will be self-explanatory

    private void calculateTotal() {
        double sum = 0;
        // for every item in : the list "items"
        for (FoodItem item : items) {
            // sum up
            sum += item.getPrice();
        }
        // set new sum
        this.totalPrice = sum;
    }

    public void addItem(FoodItem item) {
        items.add(item);
        calculateTotal(); // updates the price
    }

    public void removeItem(FoodItem item) {
        items.remove(item);
        calculateTotal();
    }

    // getters
    public List<FoodItem> getItems() { return items; }
    public double getTotalPrice() { return totalPrice; }

}

