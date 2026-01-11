package com.example;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UserDAO {

    // =====================================================
    // CREATE USER
    // =====================================================
    public boolean createUser(User user) {
        String sql =
                "INSERT INTO users (full_name, email, password_hash, role_id, status, enabled, created_at, updated_at) " +
                        "VALUES (?, LOWER(TRIM(?)), crypt(?, gen_salt('bf')), ?, ?, ?, NOW(), NOW()) RETURNING id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, user.getUsername());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPassword());
            ps.setInt(4, user.getRoleId());
            ps.setString(5, user.getStatus());
            ps.setBoolean(6, user.isEnabled());

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                user.setUserId(rs.getInt("id"));
                return true;
            }
        } catch (SQLException e) {
            System.err.println("createUser: " + e.getMessage());
        }
        return false;
    }

    // =====================================================
    // AUTHENTICATION
    // =====================================================
    public User authenticate(String email, String plainPassword) {
        String sql =
                "SELECT u.*, r.name AS role_name FROM users u " +
                        "LEFT JOIN roles r ON u.role_id = r.id " +
                        "WHERE LOWER(TRIM(u.email)) = LOWER(TRIM(?)) " +
                        "AND u.enabled = TRUE " +
                        "AND u.password_hash = crypt(?, u.password_hash)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            ps.setString(2, plainPassword);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) return extractUser(rs);

        } catch (SQLException e) {
            System.err.println("authenticate: " + e.getMessage());
        }
        return null;
    }

    // =====================================================
    // BASIC QUERIES
    // =====================================================
    public boolean emailExists(String email) {
        String sql = "SELECT 1 FROM users WHERE LOWER(TRIM(email)) = LOWER(TRIM(?))";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            return ps.executeQuery().next();
        } catch (SQLException e) {
            return false;
        }
    }

    public int getRoleIdByName(String roleName) {
        String sql = "SELECT id FROM roles WHERE UPPER(TRIM(name)) = UPPER(TRIM(?))";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, roleName);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("id");
        } catch (SQLException e) {
            return -1;
        }
        return -1;
    }

    public List<String> getRegistrationRoleNames() {
        List<String> roles = new ArrayList<>();
        String sql = "SELECT name FROM roles WHERE UPPER(TRIM(name)) <> 'ADMIN' ORDER BY name";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) roles.add(rs.getString("name"));
        } catch (SQLException ignored) {}
        return roles;
    }

    public User getUserById(int userId) {
        String sql =
                "SELECT u.*, r.name AS role_name FROM users u " +
                        "LEFT JOIN roles r ON u.role_id = r.id WHERE u.id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return extractUser(rs);
        } catch (SQLException e) {
            return null;
        }
        return null;
    }

    public List<User> getAllUsers() {
        List<User> list = new ArrayList<>();
        String sql =
                "SELECT u.*, r.name AS role_name FROM users u " +
                        "LEFT JOIN roles r ON u.role_id = r.id ORDER BY u.created_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(extractUser(rs));
        } catch (SQLException ignored) {}
        return list;
    }

    public List<User> getPendingUsers() {
        List<User> list = new ArrayList<>();
        String sql =
                "SELECT u.*, r.name AS role_name FROM users u " +
                        "LEFT JOIN roles r ON u.role_id = r.id WHERE u.status = 'PENDING'";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(extractUser(rs));
        } catch (SQLException ignored) {}
        return list;
    }

    public boolean approveUser(int userId) {
        String sql = "UPDATE users SET status = 'ACCEPTED', updated_at = NOW() WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }
    // Reject user (ADMIN action)
    public boolean rejectUser(int userId) {
        String sql = "UPDATE users SET status = 'REJECTED', updated_at = NOW() WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("rejectUser: " + e.getMessage());
            return false;
        }
    }


    public boolean deleteUser(int userId) {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    // =====================================================
    // PASSWORD RESET  (TOKEN FIXED)
    // =====================================================

    // 🔐 Create token (invalidate old ones)
    public String createResetToken(String email, int ttlMinutes) {

        String findUser = "SELECT id FROM users WHERE LOWER(TRIM(email)) = ?";
        String invalidateOld =
                "UPDATE password_reset_tokens SET used = TRUE WHERE user_id = ? AND used = FALSE";
        String insertToken =
                "INSERT INTO password_reset_tokens (user_id, token, expires_at, used) " +
                        "VALUES (?, ?, NOW() + (? || ' minutes')::interval, FALSE)";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            int userId;
            try (PreparedStatement ps = conn.prepareStatement(findUser)) {
                ps.setString(1, email);
                ResultSet rs = ps.executeQuery();
                if (!rs.next()) return null;
                userId = rs.getInt("id");
            }

            // Invalidate previous tokens
            try (PreparedStatement ps = conn.prepareStatement(invalidateOld)) {
                ps.setInt(1, userId);
                ps.executeUpdate();
            }

            // ✅ Short & copy-safe token
            String token = UUID.randomUUID()
                    .toString()
                    .replace("-", "")
                    .substring(0, 8)
                    .toUpperCase();

            try (PreparedStatement ps = conn.prepareStatement(insertToken)) {
                ps.setInt(1, userId);
                ps.setString(2, token);
                ps.setInt(3, ttlMinutes);
                ps.executeUpdate();
            }

            conn.commit();
            System.out.println("TOKEN CREATED = [" + token + "]");

            return token;

        } catch (SQLException e) {
            System.err.println("createResetToken: " + e.getMessage());
        }
        return null;
    }

    // 🔑 Apply reset
    public boolean resetPasswordByToken(String token, String newPlainPassword) {


        if (token == null || token.trim().isEmpty()) return false;

        String tok = token.replaceAll("\\s+", "").trim();
        System.out.println("TOKEN RECEIVED = [" + tok + "]");
        String select = "SELECT user_id FROM password_reset_tokens " +
                "WHERE token = ? AND used = FALSE AND expires_at > NOW()";

        String updatePwd = "UPDATE users " +
                "SET password_hash = crypt(?, gen_salt('bf')), updated_at = NOW() " +
                "WHERE id = ?";

        String markUsed = "UPDATE password_reset_tokens " +
                "SET used = TRUE, used_at = NOW() WHERE token = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            int userId;

            try (PreparedStatement sel = conn.prepareStatement(select)) {
                sel.setString(1, tok);
                ResultSet rs = sel.executeQuery();
                if (!rs.next()) {
                    conn.rollback();
                    System.err.println("❌ TOKEN INVALID / EXPIRED = [" + tok + "]");
                    return false;
                }
                userId = rs.getInt("user_id");
            }

            try (PreparedStatement up = conn.prepareStatement(updatePwd)) {
                up.setString(1, newPlainPassword);
                up.setInt(2, userId);
                if (up.executeUpdate() == 0) {
                    conn.rollback();
                    return false;
                }
            }

            try (PreparedStatement mu = conn.prepareStatement(markUsed)) {
                mu.setString(1, tok);
                mu.executeUpdate();
            }

            conn.commit();
            System.out.println("✅ PASSWORD RESET OK for user_id=" + userId);
            return true;

        } catch (SQLException e) {
            System.err.println("resetPasswordByToken ERROR: " + e.getMessage());
            return false;
        }
    }


    // =====================================================
    // MAPPER
    // =====================================================
    private User extractUser(ResultSet rs) throws SQLException {
        User u = new User();
        u.setUserId(rs.getInt("id"));
        u.setUsername(rs.getString("full_name"));
        u.setEmail(rs.getString("email"));
        u.setPassword(rs.getString("password_hash"));
        u.setRoleId(rs.getInt("role_id"));
        u.setRoleName(rs.getString("role_name"));
        u.setStatus(rs.getString("status"));
        u.setEnabled(rs.getBoolean("enabled"));
        u.setCreatedAt(rs.getTimestamp("created_at"));
        u.setUpdatedAt(rs.getTimestamp("updated_at"));
        return u;
    }
}
