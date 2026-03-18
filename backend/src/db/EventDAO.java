package db;

import model.Event;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for the events table.
 * Uses JOIN with halls and event_registrations to enrich event data.
 *
 * All SQL queries use prepared statements to prevent SQL Injection.
 */
public class EventDAO {

    /**
     * Returns all events, enriched with hall name, hall capacity,
     * and current registration count.
     * This is the main query used to display the events list.
     *
     * @return list of all events (may be empty, never null)
     * @throws SQLException if a database error occurs
     */
    public List<Event> findAll() throws SQLException {
        // Complex query: JOIN events with halls, COUNT registrations per event
        String sql =
            "SELECT e.id, e.title, e.description, e.event_date, e.event_time, " +
            "       e.hall_id, e.created_by, " +
            "       h.name AS hall_name, h.capacity AS hall_capacity, " +
            "       COUNT(r.id) AS registration_count " +
            "FROM events e " +
            "JOIN halls h ON e.hall_id = h.id " +
            "LEFT JOIN event_registrations r ON e.id = r.event_id " +
            "GROUP BY e.id, e.title, e.description, e.event_date, e.event_time, " +
            "         e.hall_id, e.created_by, h.name, h.capacity " +
            "ORDER BY e.event_date, e.event_time";

        List<Event> events = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                events.add(mapRow(rs));
            }
        }
        return events;
    }

    /**
     * Finds a single event by ID, enriched with hall info and registration count.
     *
     * @param id the event's ID
     * @return the Event object, or null if not found
     * @throws SQLException if a database error occurs
     */
    public Event findById(int id) throws SQLException {
        String sql =
            "SELECT e.id, e.title, e.description, e.event_date, e.event_time, " +
            "       e.hall_id, e.created_by, " +
            "       h.name AS hall_name, h.capacity AS hall_capacity, " +
            "       COUNT(r.id) AS registration_count " +
            "FROM events e " +
            "JOIN halls h ON e.hall_id = h.id " +
            "LEFT JOIN event_registrations r ON e.id = r.event_id " +
            "WHERE e.id = ? " +
            "GROUP BY e.id, e.title, e.description, e.event_date, e.event_time, " +
            "         e.hall_id, e.created_by, h.name, h.capacity";

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
     * Inserts a new event into the database.
     *
     * @param event the Event to insert (id is ignored, auto-generated)
     * @return the auto-generated ID of the new event
     * @throws SQLException if a database error occurs
     */
    public int insert(Event event) throws SQLException {
        String sql = "INSERT INTO events (title, description, event_date, event_time, hall_id, created_by) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, event.getTitle());
            stmt.setString(2, event.getDescription());
            stmt.setString(3, event.getEventDate());
            stmt.setString(4, event.getEventTime());
            stmt.setInt(5, event.getHallId());
            stmt.setInt(6, event.getCreatedBy());
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
     * Updates an existing event's details.
     *
     * @param event the Event with updated values (id must be set)
     * @throws SQLException if a database error occurs
     */
    public void update(Event event) throws SQLException {
        String sql = "UPDATE events SET title = ?, description = ?, event_date = ?, " +
                     "event_time = ?, hall_id = ? WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, event.getTitle());
            stmt.setString(2, event.getDescription());
            stmt.setString(3, event.getEventDate());
            stmt.setString(4, event.getEventTime());
            stmt.setInt(5, event.getHallId());
            stmt.setInt(6, event.getId());
            stmt.executeUpdate();
        }
    }

    /**
     * Deletes an event by ID.
     * All registrations for this event are automatically deleted (CASCADE).
     *
     * @param id the event ID to delete
     * @throws SQLException if a database error occurs
     */
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM events WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    /**
     * Maps a ResultSet row to an Event object, including joined hall and count fields.
     */
    private Event mapRow(ResultSet rs) throws SQLException {
        Event event = new Event(
            rs.getInt("id"),
            rs.getString("title"),
            rs.getString("description"),
            rs.getString("event_date"),
            rs.getString("event_time"),
            rs.getInt("hall_id"),
            rs.getInt("created_by")
        );
        event.setHallName(rs.getString("hall_name"));
        event.setHallCapacity(rs.getInt("hall_capacity"));
        event.setRegistrationCount(rs.getInt("registration_count"));
        return event;
    }
}
