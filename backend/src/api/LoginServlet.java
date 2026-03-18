package api;

import db.UserDAO;
import model.User;
import util.HashUtil;
import util.ValidationUtil;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;

/**
 * Handles user login and logout.
 *
 * POST /api/login   — authenticate user, create session
 * POST /api/logout  — destroy session
 * GET  /api/login   — returns current session info
 *
 * URL: /api/login
 */
@WebServlet("/api/login")
public class LoginServlet extends BaseServlet {

    private final UserDAO userDAO = new UserDAO();

    /**
     * GET /api/login — returns the currently logged-in user's info, or 401 if not logged in.
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User user = getLoggedInUser(req);
        if (user == null) {
            sendError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Not logged in.");
        } else {
            String json = "{\"id\": " + user.getId() +
                          ", \"username\": \"" + escapeJson(user.getUsername()) + "\"" +
                          ", \"role\": \"" + user.getRole() + "\"}";
            sendData(resp, json);
        }
    }

    /**
     * POST /api/login — authenticate with username + password.
     * On success: creates a session and returns user info.
     * On failure: returns 401.
     *
     * Request parameters:
     *   username (String)
     *   password (String) — plain text, hashed here before comparing
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String action   = req.getParameter("action");
        String username = req.getParameter("username");
        String password = req.getParameter("password");

        // Handle logout via POST with action=logout
        if ("logout".equals(action)) {
            HttpSession session = req.getSession(false);
            if (session != null) session.invalidate();
            sendSuccess(resp, "Logged out successfully.");
            return;
        }

        // Validate that fields are not empty
        if (!ValidationUtil.isNotEmpty(username, "username") ||
            !ValidationUtil.isNotEmpty(password, "password")) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Username and password are required.");
            return;
        }

        try {
            User user = userDAO.findByUsername(username.trim());

            // Check user exists and password matches
            if (user == null || !HashUtil.matches(password, user.getPasswordHash())) {
                sendError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Invalid username or password.");
                return;
            }

            // Create session and store user
            HttpSession session = req.getSession(true);
            session.setAttribute("user", user);
            session.setMaxInactiveInterval(60 * 60); // 1 hour

            String json = "{\"id\": " + user.getId() +
                          ", \"username\": \"" + escapeJson(user.getUsername()) + "\"" +
                          ", \"role\": \"" + user.getRole() + "\"}";
            sendData(resp, json);

        } catch (SQLException e) {
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error: " + e.getMessage());
        }
    }
}
