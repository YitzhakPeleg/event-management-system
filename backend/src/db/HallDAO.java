package db;

import model.Hall;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for the halls table.
 * Handles all database operations for hall venues.
 *
 * All SQL queries use prepared statements to prevent SQL Injection.
 */
public class HallDAO {

    /**
     * Returns all halls ordered by name.
     *
     * @return list of all halls (may be empty, never null)
     * @throws SQLException if a database error occurs
     */
    public List<Hall> findAll() throws SQLException {
        String sql = "SELECT id, name, capacity, location FROM halls ORDER BY name";
        List<Hall> halls = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                halls.add(mapRow(rs));
            }
        }
        return halls;
    }

    /**
     * Finds a hall by its ID.
     *
     * @param id the hall's ID
     * @return the Hall object, or null if not found
     * @throws SQLException if a database error occurs
     */
    public Hall findById(int id) throws SQLException {
        String sql = "SELECT id, name, capacity, location FROM halls WHERE id = ?";

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
     * Inserts a new hall into the database.
     *
     * @param hall the Hall to insert (id is ignored, auto-generated)
     * @return the auto-generated ID of the new hall
     * @throws SQLException if a database error occurs
     */
    public int insert(Hall hall) throws SQLException {
        String sql = "INSERT INTO halls (name, capacity, location) VALUES (?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, hall.getName());
            stmt.setInt(2, hall.getCapacity());
            stmt.setString(3, hall.getLocation());
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
     * Updates an existing hall's details.
     *
     * @param hall the Hall with updated values (id must be set)
     * @throws SQLException if a database error occurs
     */
    public void update(Hall hall) throws SQLException {
        String sql = "UPDATE halls SET name = ?, capacity = ?, location = ? WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, hall.getName());
            stmt.setInt(2, hall.getCapacity());
            stmt.setString(3, hall.getLocation());
            stmt.setInt(4, hall.getId());
            stmt.executeUpdate();
        }
    }

    /**
     * Deletes a hall by ID.
     * Note: will fail if events are still linked to this hall (FK constraint).
     *
     * @param id the ID of the hall to delete
     * @throws SQLException if a database error occurs or hall is in use
     */
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM halls WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    /**
     * Maps a single ResultSet row to a Hall object.
     */
    private Hall mapRow(ResultSet rs) throws SQLException {
        return new Hall(
            rs.getInt("id"),
            rs.getString("name"),
            rs.getInt("capacity"),
            rs.getString("location")
        );
    }
}
