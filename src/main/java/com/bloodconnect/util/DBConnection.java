package com.bloodconnect.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Database connection factory.
 * Reads connection details from environment variables with localhost fallbacks,
 * so the same code works on local dev and Railway without changes.
 */
public class DBConnection {

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL JDBC Driver not found", e);
        }
    }

    private static String env(String key, String fallback) {
        String v = System.getenv(key);
        return (v == null || v.isEmpty()) ? fallback : v;
    }

    public static Connection getConnection() throws SQLException {
        String host = env("MYSQLHOST", "localhost");
        String port = env("MYSQLPORT", "3306");
        String db   = env("MYSQLDATABASE", "bloodconnect");
        String user = env("MYSQLUSER", "root");
        String pass = env("MYSQLPASSWORD", "");

        String url = "jdbc:mysql://" + host + ":" + port + "/" + db
                   + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

        try {
            return DriverManager.getConnection(url, user, pass);
        } catch (SQLException e) {
            // If host is not localhost, try local database as a fallback
            if (!"localhost".equals(host)) {
                System.out.println("[DBConnection] Warning: Cloud connection to " + host + " failed. Falling back to local database...");
                String localUrl = "jdbc:mysql://localhost:3306/bloodconnect"
                                + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
                try {
                    return DriverManager.getConnection(localUrl, "root", "");
                } catch (SQLException ex) {
                    // If local fails too, throw original exception to indicate connection issue
                    throw e;
                }
            } else {
                throw e;
            }
        }
    }
}

