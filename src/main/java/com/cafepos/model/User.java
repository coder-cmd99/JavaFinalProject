package com.cafepos.model;

/** Represents a POS system user (cashier or admin). */
public class User {
    private int id;
    private String username;
    private String fullName;
    private String role;

    public User() {}

    public User(int id, String username, String fullName, String role) {
        this.id = id;
        this.username = username;
        this.fullName = fullName;
        this.role = role;
    }

    public int    getId()       { return id; }
    public String getUsername() { return username; }
    public String getFullName() { return fullName; }
    public String getRole()     { return role; }

    public void setId(int id)                { this.id = id; }
    public void setUsername(String username) { this.username = username; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setRole(String role)         { this.role = role; }

    public boolean isAdmin() { return "admin".equalsIgnoreCase(role); }

    @Override public String toString() {
        return fullName + " (" + role + ")";
    }
}
