package com.files.projBistro.models.userModel;

public class User {
    // for a User object, have expected
    // fields
    private String username;
    private String password;
    private Role role; // check ENUM file

    // constructor
    public User(String username, String password, Role role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }

    // getters
    public String getUsername() { return username; }
    public Role getRole() { return role; }

    // 2a. role checker
    public boolean isAdmin(){
        /*
         check operating object's role
         ""does this object's role (str. value) == Role's "ADMIN" str.?""
         return true if equal
        */
        return this.role == Role.ADMIN;
    }



}
