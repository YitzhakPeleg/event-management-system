package db;

import model.Registration;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for the event_registrations table.
 * This is the linking table between events and participants.
 *
 * All SQL queries use prepared statements to prevent SQL Injection.
 */
public class RegistrationDAO {

    /**
     * Returns all registrations for a specific event,
     * including participant name and event title (via JOIN).
     *
     * @param eventId the event's ID
     * @return list of registrations for that event
     * @throws SQLException if a database error occurs
     */
    public List<Registration> findByEventId(int eventId) throws SQLException {
        // Complex query: JOIN registrations with participants and events
        String sql =
            "SELECT r.id, r.event_id, r.participant_id, r.registered_at, r.qr_code_token, " +
            "       e.title AS event_title, e.event_date, " +
            "       CONCAT(p.first_name, ' ', p.last_name) AS participant_name " +
            "FROM event_registrations r " +
            "JOIN events e ON r.event_id = e.id " +
            "JOIN participants p ON r.participant_id = p.id " +
            "WHERE r.event_id = ? " +
            "ORDER BY r.registered_at";

        List<Registration> list = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, eventId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    /**
     * Returns all registrations belonging to a specific participant,
     * including event title and date (via JOIN).
     *
     * @param participantId the participant's ID
     * @return list of registrations for that participant
     * @throws SQLException if a database error occurs
     */
    public List<Registration> findByParticipantId(int participantId) throws SQLException {
        String sql =
            "SELECT r.id, r.event_id, r.participant_id, r.registered_at, r.qr_code_token, " +
            "       e.title AS event_title, e.event_date, " +
            "       CONCAT(p.first_name, ' ', p.last_name) AS participant_name " +
            "FROM event_registrations r " +
            "JOIN events e ON r.event_id = e.id " +
            "JOIN participants p ON r.participant_id = p.id " +
            "WHERE r.participant_id = ? " +
            "ORDER BY e.event_date";

        List<Registration> list = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, participantId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    /**
     * Checks if a participant is already registered to a specific event.
     * Used before inserting to prevent duplicate registrations.
     *
     * @param eventId       the event ID
     * @param participantId the participant ID
     * @return true if already registered
     * @throws SQLException if a database error occurs
     */
    public boolean exists(int eventId, int participantId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM event_registrations WHERE event_id = ? AND participant_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, eventId);
            stmt.setInt(2, participantId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    /**
     * Counts how many participants are registered to a specific event.
     * Used to check hall capacity before allowing a new registration.
     *
     * @param eventId the event ID
     * @return the number of current registrations
     * @throws SQLException if a database error occurs
     */
    public int countByEventId(int eventId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM event_registrations WHERE event_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, eventId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    /**
     * Finds a registration by its QR code token.
     * Used at the event entrance when scanning a QR code.
     *
     * @param token the unique QR code token
     * @return the Registration, or null if not found
     * @throws SQLException if a database error occurs
     */
    public Registration findByToken(String token) throws SQLException {
        String sql =
            "SELECT r.id, r.event_id, r.participant_id, r.registered_at, r.qr_code_token, " +
            "       e.title AS event_title, e.event_date, " +
            "       CONCAT(p.first_name, ' ', p.last_name) AS participant_name " +
            "FROM event_registrations r " +
            "JOIN events e ON r.event_id = e.id " +
            "JOIN participants p ON r.participant_id = p.id " +
            "WHERE r.qr_code_token = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, token);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    /**
     * Inserts a new registration.
     *
     * @param registration the Registration to insert (id is ignored, auto-generated)
     * @return the auto-generated ID of the new registration
     * @throws SQLException if a database error occurs (e.g. duplicate entry)
     */
    public int insert(Registration registration) throws SQLException {
        String sql = "INSERT INTO event_registrations (event_id, participant_id, qr_code_token) " +
                     "VALUES (?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, registration.getEventId());
            stmt.setInt(2, registration.getParticipantId());
            stmt.setString(3, registration.getQrCodeToken());
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
     * Deletes a registration by ID (cancel registration).
     *
     * @param id the registration ID to delete
     * @throws SQLException if a database error occurs
     */
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM event_registrations WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    /**
     * Maps a ResultSet row to a Registration object, including joined fields.
     */
    private Registration mapRow(ResultSet rs) throws SQLException {
        Registration reg = new Registration(
            rs.getInt("id"),
            rs.getInt("event_id"),
            rs.getInt("participant_id"),
            rs.getString("registered_at"),
            rs.getString("qr_code_token")
        );
        reg.setEventTitle(rs.getString("event_title"));
        reg.setEventDate(rs.getString("event_date"));
        reg.setParticipantName(rs.getString("participant_name"));
        return reg;
    }
}
