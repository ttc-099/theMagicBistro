package com.files.projBistro.models.userModel;

public class User {
    private int id; // Added to match database user_id
    private String username;
    private String password;
    private Role role;
    private String phoneNumber;

    // --- NEW CONSTRUCTOR FOR THE DAO ---
    // This allows the database to create a User object using Strings
    public User(int id, String username, String roleName, String phoneNumber) {
        this.id = id;
        this.username = username;
        // This converts the String "Admin" or "Customer" from SQL
        // into your actual Role Enum (Role.ADMIN or Role.CUSTOMER)
        this.role = Role.valueOf(roleName.toUpperCase());
        this.phoneNumber = phoneNumber;
    }

    // --- YOUR ORIGINAL CONSTRUCTOR ---
    public User(String username, String password, Role role, String phoneNumber) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.phoneNumber = phoneNumber;
    }

    // getters
    public String getUsername() { return username; }
    public Role getRole() { return role; }
    public int getId() { return id; }
    public String getPhoneNumber() { return phoneNumber; }

    // role checker
    public boolean isAdmin(){
        return this.role == Role.ADMIN;
    }
}