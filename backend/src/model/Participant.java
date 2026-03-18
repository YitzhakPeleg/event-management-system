package model;

/**
 * Represents a participant (a regular user's personal details).
 * email and phone are stored encrypted in the database (AES).
 * When read from the DB they are decrypted by EncryptionUtil before
 * being placed in this object, so the fields here always hold plain text.
 */
public class Participant {

    // ---- Fields ----
    private int id;
    private String firstName;
    private String lastName;
    private String email;   // plain text (decrypted before storing here)
    private String phone;   // plain text (decrypted before storing here)
    private int userId;     // links to users table

    // ---- Constructors ----

    public Participant() {}

    public Participant(int id, String firstName, String lastName,
                       String email, String phone, int userId) {
        this.id        = id;
        this.firstName = firstName;
        this.lastName  = lastName;
        this.email     = email;
        this.phone     = phone;
        this.userId    = userId;
    }

    // ---- Getters and Setters ----

    public int getId()               { return id; }
    public void setId(int id)        { this.id = id; }

    public String getFirstName()                  { return firstName; }
    public void setFirstName(String firstName)    { this.firstName = firstName; }

    public String getLastName()                 { return lastName; }
    public void setLastName(String lastName)    { this.lastName = lastName; }

    public String getEmail()              { return email; }
    public void setEmail(String email)    { this.email = email; }

    public String getPhone()              { return phone; }
    public void setPhone(String phone)    { this.phone = phone; }

    public int getUserId()               { return userId; }
    public void setUserId(int userId)    { this.userId = userId; }

    /** Returns the full name as "First Last". */
    public String getFullName() {
        return firstName + " " + lastName;
    }

    @Override
    public String toString() {
        return "Participant{id=" + id + ", name=" + getFullName() + ", userId=" + userId + "}";
    }
}
