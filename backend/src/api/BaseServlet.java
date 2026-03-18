package api;

import model.User;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Base class for all API Servlets.
 * Provides helper methods for:
 *   - Writing JSON responses
 *   - Checking login and admin permissions
 *
 * All API Servlets extend this class instead of HttpServlet directly.
 */
public abstract class BaseServlet extends HttpServlet {

    /**
     * Writes a JSON success response with a message field.
     * Example: {"success": true, "message": "Created successfully"}
     */
    protected void sendSuccess(HttpServletResponse resp, String message) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.setStatus(HttpServletResponse.SC_OK);
        PrintWriter out = resp.getWriter();
        out.print("{\"success\": true, \"message\": \"" + escapeJson(message) + "\"}");
        out.flush();
    }

    /**
     * Writes a JSON success response with arbitrary JSON data.
     * Example: {"success": true, "data": { ... }}
     */
    protected void sendData(HttpServletResponse resp, String jsonData) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.setStatus(HttpServletResponse.SC_OK);
        PrintWriter out = resp.getWriter();
        out.print("{\"success\": true, \"data\": " + jsonData + "}");
        out.flush();
    }

    /**
     * Writes a JSON error response with an error message.
     * Example: {"success": false, "error": "Not authorized"}
     */
    protected void sendError(HttpServletResponse resp, int statusCode, String message) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.setStatus(statusCode);
        PrintWriter out = resp.getWriter();
        out.print("{\"success\": false, \"error\": \"" + escapeJson(message) + "\"}");
        out.flush();
    }

    /**
     * Returns the currently logged-in user from the session, or null if not logged in.
     */
    protected User getLoggedInUser(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) return null;
        return (User) session.getAttribute("user");
    }

    /**
     * Checks if a user is logged in. Sends a 401 error and returns false if not.
     */
    protected boolean requireLogin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (getLoggedInUser(req) == null) {
            sendError(resp, HttpServletResponse.SC_UNAUTHORIZED, "You must be logged in.");
            return false;
        }
        return true;
    }

    /**
     * Checks if the logged-in user is an admin. Sends a 403 error and returns false if not.
     */
    protected boolean requireAdmin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User user = getLoggedInUser(req);
        if (user == null) {
            sendError(resp, HttpServletResponse.SC_UNAUTHORIZED, "You must be logged in.");
            return false;
        }
        if (!user.isAdmin()) {
            sendError(resp, HttpServletResponse.SC_FORBIDDEN, "Admin access required.");
            return false;
        }
        return true;
    }

    /**
     * Escapes special characters in a string for safe inclusion in JSON.
     */
    protected String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }
}
