package com.bloodconnect.util;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Automatically checks and initializes the MySQL database schema on application startup.
 * Avoids manual DB CLI setup.
 */
@WebListener
public class DatabaseInitializer implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("DatabaseInitializer: Checking database tables...");

        try (Connection conn = DBConnection.getConnection()) {
            boolean tablesExist = false;
            try {
                // Check if users table exists by running a simple query
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT 1 FROM users LIMIT 1")) {
                    tablesExist = true;
                    System.out.println("DatabaseInitializer: 'users' table exists. Skipping initialization.");
                }
            } catch (SQLException e) {
                // Table doesn't exist, need to run the schema script
                System.out.println("DatabaseInitializer: 'users' table not found. Initializing database schema...");
            }

            if (!tablesExist) {
                InputStream is = sce.getServletContext().getResourceAsStream("/WEB-INF/schema.sql");
                if (is == null) {
                    System.err.println("DatabaseInitializer ERROR: /WEB-INF/schema.sql file not found!");
                    return;
                }

                try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                    StringBuilder sqlBuilder = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        // Ignore SQL comments and empty lines
                        if (line.trim().startsWith("--") || line.trim().isEmpty()) {
                            continue;
                        }
                        sqlBuilder.append(line).append("\n");
                    }

                    // Split statements by semicolon
                    String[] statements = sqlBuilder.toString().split(";");
                    try (Statement stmt = conn.createStatement()) {
                        for (String sql : statements) {
                            if (!sql.trim().isEmpty()) {
                                stmt.execute(sql.trim());
                            }
                        }
                        System.out.println("DatabaseInitializer: Database schema initialized successfully!");
                    }
                } catch (Exception e) {
                    System.err.println("DatabaseInitializer ERROR: Failed to read/execute schema.sql");
                    e.printStackTrace();
                }
            }

        } catch (SQLException e) {
            System.err.println("DatabaseInitializer ERROR: Failed to connect to database for initialization!");
            e.printStackTrace();
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // No cleanup needed
    }
}
