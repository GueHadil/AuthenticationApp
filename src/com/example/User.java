package com.example;

import java.sql.Timestamp;

/**
 * Modèle utilisateur correspondant à la table users.
 */
public class User {
    private int userId;
    private String username; // full_name
    private String email;
    private String password; // plain lors de création / update, hash stocké DB
    private int roleId;
    private String roleName;
    private String status; // PENDING, ACCEPTED, REJECTED
    private boolean enabled;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public User() {
        this.status = "PENDING";
        this.enabled = true;
    }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username != null ? username.trim() : null; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email != null ? email.trim().toLowerCase() : null; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public int getRoleId() { return roleId; }
    public void setRoleId(int roleId) { this.roleId = roleId; }
    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return "User{id=" + userId +
                ", name='" + username + '\'' +
                ", email='" + email + '\'' +
                ", role=" + (roleName != null ? roleName : roleId) +
                ", status=" + status +
                ", enabled=" + enabled + '}';
    }
}