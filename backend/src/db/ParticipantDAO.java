package db;

import model.Participant;
import util.DBConnection;
import util.EncryptionUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for the participants table.
 *
 * IMPORTANT: email and phone are encrypted before INSERT/UPDATE,
 * and decrypted after SELECT. This class handles encryption/decryption
 * transparently — callers always work with plain text values.
 *
 * All SQL queries use prepared statements to prevent SQL Injection.
 */
public class ParticipantDAO {

    /**
     * Returns all participants. Email and phone are decrypted.
     *
     * @return list of all participants (may be empty, never null)
     * @throws SQLException if a database error occurs
     */
    public List<Participant> findAll() throws SQLException {
        String sql = "SELECT id, first_name, last_name, email_encrypted, phone_encrypted, user_id " +
                     "FROM participants ORDER BY last_name, first_name";
        List<Participant> list = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    /**
     * Finds a participant by their ID. Email and phone are decrypted.
     *
     * @param id the participant's ID
     * @return the Participant object, or null if not found
     * @throws SQLException if a database error occurs
     */
    public Participant findById(int id) throws SQLException {
        String sql = "SELECT id, first_name, last_name, email_encrypted, phone_encrypted, user_id " +
                     "FROM participants WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    /**
     * Finds the participant linked to a specific user account.
     * Each user has exactly one participant profile.
     *
     * @param userId the user's ID
     * @return the Participant, or null if not found
     * @throws SQLException if a database error occurs
     */
    public Participant findByUserId(int userId) throws SQLException {
        String sql = "SELECT id, first_name, last_name, email_encrypted, phone_encrypted, user_id " +
                     "FROM participants WHERE user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    /**
     * Inserts a new participant. Email and phone are encrypted before saving.
     *
     * @param participant the Participant to insert (id is ignored, auto-generated)
     * @return the auto-generated ID of the new participant
     * @throws SQLException if a database error occurs
     */
    public int insert(Participant participant) throws SQLException {
        String sql = "INSERT INTO participants (first_name, last_name, email_encrypted, phone_encrypted, user_id) " +
                     "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, participant.getFirstName());
            stmt.setString(2, participant.getLastName());
            stmt.setString(3, EncryptionUtil.encrypt(participant.getEmail()));  // encrypt before save
            stmt.setString(4, EncryptionUtil.encrypt(participant.getPhone()));  // encrypt before save
            stmt.setInt(5, participant.getUserId());
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        throw new SQLException("Insert failed, no ID returned.");
    }

    /**
     * Updates a participant's details. Email and phone are encrypted before saving.
     *
     * @param participant the Participant with updated values (id must be set)
     * @throws SQLException if a database error occurs
     */
    public void update(Participant participant) throws SQLException {
        String sql = "UPDATE participants SET first_name = ?, last_name = ?, " +
                     "email_encrypted = ?, phone_encrypted = ? WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, participant.getFirstName());
            stmt.setString(2, participant.getLastName());
            stmt.setString(3, EncryptionUtil.encrypt(participant.getEmail()));
            stmt.setString(4, EncryptionUtil.encrypt(participant.getPhone()));
            stmt.setInt(5, participant.getId());
            stmt.executeUpdate();
        }
    }

    /**
     * Maps a ResultSet row to a Participant. Decrypts email and phone.
     */
    private Participant mapRow(ResultSet rs) throws SQLException {
        return new Participant(
            rs.getInt("id"),
            rs.getString("first_name"),
            rs.getString("last_name"),
            EncryptionUtil.decrypt(rs.getString("email_encrypted")),  // decrypt on read
            EncryptionUtil.decrypt(rs.getString("phone_encrypted")),  // decrypt on read
            rs.getInt("user_id")
        );
    }
}
