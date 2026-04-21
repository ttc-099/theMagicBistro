package com.files.projBistro.controllers;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LoginControllerTest {

    // Empty field check
    private boolean isAnyFieldEmpty(String username, String password) {
        return username == null || username.trim().isEmpty()
                || password == null || password.trim().isEmpty();
    }

    @Test
    void testEmptyFields_ReturnsTrueWhenEmpty() {
        assertTrue(isAnyFieldEmpty("", "pass"));
        assertTrue(isAnyFieldEmpty("user", ""));
        assertTrue(isAnyFieldEmpty("", ""));
        assertTrue(isAnyFieldEmpty(null, "pass"));
    }

    @Test
    void testEmptyFields_ReturnsFalseWhenBothFilled() {
        assertFalse(isAnyFieldEmpty("user", "pass"));
        assertFalse(isAnyFieldEmpty("  user  ", "  pass  "));
    }

    // Username length validation (same as in RegisterController)
    private boolean isValidUsername(String username) {
        return username != null && username.length() >= 1 && username.length() <= 50;
    }

    @Test
    void testUsernameLength_Valid() {
        assertTrue(isValidUsername("a"));
        assertTrue(isValidUsername("a".repeat(50)));
        assertTrue(isValidUsername("john"));
    }

    @Test
    void testUsernameLength_Invalid() {
        assertFalse(isValidUsername(""));
        assertFalse(isValidUsername(null));
        assertFalse(isValidUsername("a".repeat(51)));
    }
}