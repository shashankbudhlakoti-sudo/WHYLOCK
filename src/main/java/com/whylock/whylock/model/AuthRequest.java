package com.whylock.whylock.model;

public class AuthRequest {

    private String username;
    private String email;      // NEW
    private String password;
    private String role;

    // ===========================
    // Username
    // ===========================
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    // ===========================
    // Email
    // ===========================
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    // ===========================
    // Password
    // ===========================
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    // ===========================
    // Role
    // ===========================
    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}