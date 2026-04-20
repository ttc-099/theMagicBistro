package com.files.projBistro.models;

import javafx.scene.Scene;

public class ThemeManager {
    private static String currentTheme = "/styles/light.css";
    private static final String DEFAULT_THEME = "/styles/light.css";

    public static void setTheme(String themePath) {
        currentTheme = themePath;
    }

    public static String getTheme() {
        return currentTheme;
    }

    public static void resetToDefault() {
        currentTheme = DEFAULT_THEME;
    }

    // ⭐ THE MAGIC HAPPENS HERE - ONE METHOD TO RULE THEM ALL ⭐
    public static void applyToScene(Scene scene, Class<?> controllerClass) {
        if (scene == null) return;

        // Remove ALL custom themes (light.css and dark.css)
        scene.getStylesheets().removeIf(sheet ->
                sheet.contains("light.css") || sheet.contains("dark.css")
        );

        // Add ONLY the current theme
        if (currentTheme != null) {
            try {
                String themeCss = controllerClass.getResource(currentTheme).toExternalForm();
                scene.getStylesheets().add(themeCss);
            } catch (Exception e) {
                System.err.println("Could not load theme: " + currentTheme);
                // Fallback to light.css if error
                try {
                    String fallback = controllerClass.getResource(DEFAULT_THEME).toExternalForm();
                    scene.getStylesheets().add(fallback);
                } catch (Exception ex) {
                    System.err.println("Could not load fallback theme either!");
                }
            }
        }
    }
}