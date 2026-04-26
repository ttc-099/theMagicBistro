package com.files.projBistro.models.dao;

import java.sql.Connection;
import java.sql.DriverManager;

public class TestDB {
    public static void main(String[] args) {
        try {
            Class.forName("org.sqlite.JDBC");
            Connection conn = DriverManager.getConnection("jdbc:sqlite:bistroTrue.db");
            System.out.println("✅ DATABASE CONNECTION SUCCESSFUL!");
        } catch (Exception e) {
            System.out.println("❌ CONNECTION FAILED: Check your Libraries/Dependencies.");
            e.printStackTrace();
        }
    }
}