package com.files.projBistro.models;

public class ThemeManager {
    private static String currentTheme = "/styles/light.css";
    private static final String DEFAULT_THEME = "/styles/light.css";

    public static void setTheme(String themePath) {
        currentTheme = themePath;
    }

    public static String getTheme() {
        return currentTheme;
    }

    // New method: reset to light mode
    public static void resetToDefault() {
        currentTheme = DEFAULT_THEME;
    }
}