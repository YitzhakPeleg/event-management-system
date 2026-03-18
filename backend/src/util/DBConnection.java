package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Manages the single JDBC connection to the MySQL database.
 * Uses the Singleton pattern so only one connection is open at a time.
 *
 * Configuration:
 *   - Change DB_URL, DB_USER, DB_PASSWORD to match your local MySQL setup.
 *   - Make sure the MySQL JDBC driver (mysql-connector-j-x.x.x.jar) is in the classpath.
 */
public class DBConnection {

    // ---- Database connection settings ----
    private static final String DB_URL      = "jdbc:mysql://localhost:3306/event_management?useSSL=false&serverTimezone=UTC";
    private static final String DB_USER     = "root";
    private static final String DB_PASSWORD = "";  // no password (default Homebrew MySQL install)

    // The single shared connection instance
    private static Connection connection = null;

    // Private constructor — prevents creating instances of this class
    private DBConnection() {}

    /**
     * Returns the shared database connection.
     * Opens a new connection if none exists or if the current one is closed.
     *
     * @return an open Connection object
     * @throws SQLException if the connection cannot be established
     */
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                // Load the MySQL JDBC driver
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                System.out.println("Database connection established.");
            } catch (ClassNotFoundException e) {
                throw new SQLException("MySQL JDBC driver not found. Add mysql-connector-j to classpath.", e);
            }
        }
        return connection;
    }

    /**
     * Closes the database connection.
     * Call this when the application shuts down.
     */
    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
                System.out.println("Database connection closed.");
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }
    }
}
