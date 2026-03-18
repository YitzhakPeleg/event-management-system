package model;

/**
 * Represents a system user.
 * A user can be an ADMIN (manages events/halls) or a regular USER (registers to events).
 */
public class User {

    // ---- Fields ----
    private int id;
    private String username;
    private String passwordHash;  // SHA-256 hash, never plain text
    private String role;          // "ADMIN" or "USER"
    private String createdAt;

    // ---- Constructors ----

    public User() {}

    public User(int id, String username, String passwordHash, String role, String createdAt) {
        this.id           = id;
        this.username     = username;
        this.passwordHash = passwordHash;
        this.role         = role;
        this.createdAt    = createdAt;
    }

    // ---- Getters and Setters ----

    public int getId()                    { return id; }
    public void setId(int id)             { this.id = id; }

    public String getUsername()                     { return username; }
    public void setUsername(String username)         { this.username = username; }

    public String getPasswordHash()                  { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getRole()              { return role; }
    public void setRole(String role)     { this.role = role; }

    public String getCreatedAt()                 { return createdAt; }
    public void setCreatedAt(String createdAt)   { this.createdAt = createdAt; }

    /** Returns true if this user has administrator privileges. */
    public boolean isAdmin() {
        return "ADMIN".equals(this.role);
    }

    @Override
    public String toString() {
        return "User{id=" + id + ", username=" + username + ", role=" + role + "}";
    }
}
