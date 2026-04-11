package com.files.projBistro.models;

public class ThemeManager {
    // default to light
    private static String currentTheme = "/styles/light.css";

    public static void setTheme(String themePath) {
        currentTheme = themePath;
    }

    public static String getTheme() {
        return currentTheme;
    }
}