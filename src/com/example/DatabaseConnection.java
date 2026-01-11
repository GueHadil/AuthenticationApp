package com.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.io.InputStream;
import java.util.Properties;


/**
 * Database Connection Manager for PostgreSQL
 */
public class DatabaseConnection {
    private static String URL;
    private static String USERNAME;
    private static String PASSWORD;
    private static String DRIVER;

    static {
        loadProperties();
    }

    private static void loadProperties() {
        try (InputStream input = DatabaseConnection.class.getClassLoader()
                .getResourceAsStream("database.properties")) {

            Properties prop = new Properties();
            if (input != null) {
                prop.load(input);
                URL = prop.getProperty("db.url");
                USERNAME = prop.getProperty("db.username");
                PASSWORD = prop.getProperty("db.password");
                DRIVER = prop.getProperty("db.driver");

                // Load PostgreSQL driver
                Class.forName(DRIVER);
                System.out.println("✅ PostgreSQL Driver loaded successfully");
            } else {
                // Default configuration if properties file not found
                URL = "jdbc:postgresql://localhost:5432/cloud_db";
                USERNAME = "postgres";
                PASSWORD = "pswd";
                DRIVER = "org.postgresql.Driver";
                Class.forName(DRIVER);
                System.out.println("⚠️ Using default database configuration");
            }
        } catch (Exception e) {
            System.err.println("❌ Error loading database configuration: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Get a database connection
     */
    public static Connection getConnection() throws SQLException {
        try {
            Connection conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            System.out.println("✅ Database connection established");
            return conn;
        } catch (SQLException e) {
            System.err.println("❌ Failed to connect to database: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Close database connection
     */
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
                System.out.println("✅ Database connection closed");
            } catch (SQLException e) {
                System.err.println("⚠️ Error closing connection: " + e.getMessage());
            }
        }
    }

    /**
     * Test database connection
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("❌ Database connection test failed: " + e.getMessage());
            return false;
        }
    }
}

