package com.files.projBistro.models.userModel;

public enum Role {
    ADMIN, CUSTOMER;

    // comment here
    public boolean equalsIgnoreCase(String roleString) {
        // check if input is null first to avoid crashes
        if (roleString == null) return false;

        // compare the enum name to the string regardless of caps
        return this.name().equalsIgnoreCase(roleString);
    }
}