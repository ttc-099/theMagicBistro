package com.files.projBistro.models.models;

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

    public static void applyToScene(Scene scene, Class<?> controllerClass) {
        if (scene == null) return;

        System.out.println("Applying theme to scene: " + currentTheme);

        // Remove old themes
        scene.getStylesheets().removeIf(sheet ->
                sheet.contains("light.css") || sheet.contains("dark.css")
        );

        // Add current theme
        if (currentTheme != null) {
            try {
                String themeCss = controllerClass.getResource(currentTheme).toExternalForm();
                scene.getStylesheets().add(themeCss);
                System.out.println("Added CSS: " + themeCss);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}