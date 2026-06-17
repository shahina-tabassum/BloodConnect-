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

    /**
     * Returns a new database connection.
     * Caller is responsible for closing it (use try-with-resources).
     */
    public static Connection getConnection() throws SQLException {
        String host = env("MYSQLHOST", "localhost");
        String port = env("MYSQLPORT", "3306");
        String db   = env("MYSQLDATABASE", "bloodconnect");
        String user = env("MYSQLUSER", "root");
        String pass = env("MYSQLPASSWORD", "");

        String url = "jdbc:mysql://" + host + ":" + port + "/" + db
                   + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

        return DriverManager.getConnection(url, user, pass);
    }
}
