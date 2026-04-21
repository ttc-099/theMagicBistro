package com.files.projBistro.controllers;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RegControllerTest {

    // ---------- Phone validation (Malaysian format) ----------
    private boolean isValidPhone(String phone) {
        return phone != null && phone.matches("0\\d{9,10}");
    }

    @Test
    void testPhoneValidation_ValidNumbers() {
        assertTrue(isValidPhone("0123456789"));
        assertTrue(isValidPhone("01234567890"));
        assertTrue(isValidPhone("0198765432"));
        assertTrue(isValidPhone("01123456789"));
    }

    @Test
    void testPhoneValidation_InvalidNumbers() {
        assertFalse(isValidPhone("123456789"));      // doesn't start with 0
        assertFalse(isValidPhone("012345"));         // too short
        assertFalse(isValidPhone("012345678901"));   // too long (12 digits)
        assertFalse(isValidPhone("01234abc89"));     // letters
        assertFalse(isValidPhone(null));
        assertFalse(isValidPhone(""));
    }

    // ---------- Username length validation ----------
    private boolean isValidUsername(String username) {
        return username != null && username.length() >= 3 && username.length() <= 50;
    }

    @Test
    void testUsernameValidation_Valid() {
        assertTrue(isValidUsername("abc"));
        assertTrue(isValidUsername("a".repeat(50)));
        assertTrue(isValidUsername("JohnDoe123"));
    }

    @Test
    void testUsernameValidation_Invalid() {
        assertFalse(isValidUsername("ab"));                 // too short
        assertFalse(isValidUsername("a".repeat(51)));       // too long
        assertFalse(isValidUsername(null));
        assertFalse(isValidUsername(""));
    }

    // ---------- Password length validation ----------
    private boolean isValidPassword(String password) {
        return password != null && password.length() >= 4 && password.length() <= 100;
    }

    @Test
    void testPasswordValidation_Valid() {
        assertTrue(isValidPassword("1234"));
        assertTrue(isValidPassword("a".repeat(100)));
        assertTrue(isValidPassword("strongPass99"));
    }

    @Test
    void testPasswordValidation_Invalid() {
        assertFalse(isValidPassword("123"));                // too short
        assertFalse(isValidPassword("a".repeat(101)));      // too long
        assertFalse(isValidPassword(null));
        assertFalse(isValidPassword(""));
    }

    // ---------- All fields required ----------
    private boolean allFieldsPresent(String username, String password, String phone) {
        return username != null && !username.trim().isEmpty()
                && password != null && !password.trim().isEmpty()
                && phone != null && !phone.trim().isEmpty();
    }

    @Test
    void testAllFieldsPresent_Valid() {
        assertTrue(allFieldsPresent("user", "pass", "0123456789"));
    }

    @Test
    void testAllFieldsPresent_MissingFields() {
        assertFalse(allFieldsPresent("", "pass", "0123456789"));
        assertFalse(allFieldsPresent("user", "", "0123456789"));
        assertFalse(allFieldsPresent("user", "pass", ""));
        assertFalse(allFieldsPresent(null, "pass", "0123456789"));
    }
}