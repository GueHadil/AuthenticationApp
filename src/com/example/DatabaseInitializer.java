package com.example;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;

/**
 * Database initialization script
 * Creates tables and inserts default data
 */
public class DatabaseInitializer {

    public static void initializeDatabase() {
        System.out.println("🔧 Initializing database...");

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            // Create roles table
            String createRolesTable =
                    "CREATE TABLE IF NOT EXISTS roles (" +
                            "  id SERIAL PRIMARY KEY," +
                            "  name VARCHAR(50) UNIQUE NOT NULL" +
                            ")";
            stmt.execute(createRolesTable);
            System.out.println("✅ Roles table created/verified");

            // Insert default roles
            String insertRoles =
                    "INSERT INTO roles(name) VALUES " +
                            "  ('ADMIN'), ('GESTIONNAIRE'), ('TRAVAILLEUR'), ('ENSEIGNANT'), ('ETUDIANT') " +
                            "ON CONFLICT (name) DO NOTHING";
            int rolesInserted = stmt.executeUpdate(insertRoles);
            System.out.println("✅ Default roles inserted: " + rolesInserted + " new role(s)");

            // Create users table
            String createUsersTable =
                    "CREATE TABLE IF NOT EXISTS users (" +
                            "  id SERIAL PRIMARY KEY," +
                            "  full_name VARCHAR(150) NOT NULL," +
                            "  email VARCHAR(150) UNIQUE NOT NULL," +
                            "  password_hash VARCHAR(200) NOT NULL," +
                            "  role_id INT NOT NULL REFERENCES roles(id)," +
                            "  status VARCHAR(20) NOT NULL DEFAULT 'PENDING'," +
                            "  enabled BOOLEAN NOT NULL DEFAULT TRUE," +
                            "  created_at TIMESTAMP NOT NULL DEFAULT NOW()," +
                            "  updated_at TIMESTAMP NOT NULL DEFAULT NOW()" +
                            ")";
            stmt.execute(createUsersTable);
            System.out.println("✅ Users table created/verified");

            // Create index on status
            String createStatusIndex =
                    "CREATE INDEX IF NOT EXISTS idx_users_status ON users(status)";
            stmt.execute(createStatusIndex);
            System.out.println("✅ Status index created/verified");

            // Create password reset tokens table
            String createTokensTable =
                    "CREATE TABLE IF NOT EXISTS password_reset_tokens (" +
                            "  id SERIAL PRIMARY KEY," +
                            "  user_id INT NOT NULL REFERENCES users(id) ON DELETE CASCADE," +
                            "  token VARCHAR(100) UNIQUE NOT NULL," +
                            "  expires_at TIMESTAMP NOT NULL," +
                            "  used BOOLEAN NOT NULL DEFAULT FALSE," +
                            "  created_at TIMESTAMP NOT NULL DEFAULT NOW()" +
                            ")";
            stmt.execute(createTokensTable);
            System.out.println("✅ Password reset tokens table created/verified");

            // Insert default admin user if not exists
            UserDAO userDAO = new UserDAO();
            if (!userDAO.emailExists("admin@example.com")) {
                int adminRoleId = userDAO.getRoleIdByName("ADMIN");
                User admin = new User();
                admin.setUsername("Administrator");
                admin.setEmail("admin@example.com");
                admin.setPassword("admin123");
                admin.setRoleId(adminRoleId);
                admin.setStatus("ACTIVE");
                admin.setEnabled(true);

                if (userDAO.createUser(admin)) {
                    System.out.println("✅ Default admin user created");
                }
            }

            // Insert default test user if not exists
            if (!userDAO.emailExists("john@example.com")) {
                int etudiantRoleId = userDAO.getRoleIdByName("ETUDIANT");
                User testUser = new User();
                testUser.setUsername("John Doe");
                testUser.setEmail("john@example.com");
                testUser.setPassword("pass123");
                testUser.setRoleId(etudiantRoleId);
                testUser.setStatus("ACTIVE");
                testUser.setEnabled(true);

                if (userDAO.createUser(testUser)) {
                    System.out.println("✅ Default test user created");
                }
            }

            System.out.println("✅ Database initialization complete!");

        } catch (SQLException e) {
            System.err.println("❌ Database initialization failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        System.out.println("===========================================");
        System.out.println("  Database Initialization Tool");
        System.out.println("===========================================\n");

        // Test connection first
        if (DatabaseConnection.testConnection()) {
            System.out.println("✅ Database connection successful!\n");
            initializeDatabase();
        } else {
            System.err.println("❌ Cannot connect to database!");
            System.err.println("Please ensure:");
            System.err.println("  1. PostgreSQL is running");
            System.err.println("  2. Database 'cloud_db' exists");
            System.err.println("  3. Username and password are correct");
        }
    }
}

