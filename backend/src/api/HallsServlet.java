package api;

import db.HallDAO;
import model.Hall;
import util.ValidationUtil;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * API Servlet for managing halls (venues).
 *
 * GET    /api/halls         — list all halls (any logged-in user)
 * POST   /api/halls         — create new hall (ADMIN only)
 * PUT    /api/halls?id=X   — update hall (ADMIN only)
 * DELETE /api/halls?id=X   — delete hall (ADMIN only)
 */
@WebServlet("/api/halls")
public class HallsServlet extends BaseServlet {

    private final HallDAO hallDAO = new HallDAO();

    /** GET — returns all halls. */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (!requireLogin(req, resp)) return;

        try {
            List<Hall> halls = hallDAO.findAll();
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < halls.size(); i++) {
                sb.append(hallToJson(halls.get(i)));
                if (i < halls.size() - 1) sb.append(",");
            }
            sb.append("]");
            sendData(resp, sb.toString());

        } catch (SQLException e) {
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error.");
        }
    }

    /** POST — creates a new hall. ADMIN only. Parameters: name, capacity, location */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (!requireAdmin(req, resp)) return;

        try {
            String name     = req.getParameter("name");
            int capacity    = Integer.parseInt(req.getParameter("capacity"));
            String location = req.getParameter("location");

            if (!ValidationUtil.isNotEmpty(name, "name"))
                throw new IllegalArgumentException("Hall name is required.");
            if (!ValidationUtil.isValidCapacity(capacity))
                throw new IllegalArgumentException("Capacity must be greater than zero.");
            if (!ValidationUtil.isNotEmpty(location, "location"))
                throw new IllegalArgumentException("Location is required.");

            Hall hall = new Hall(0, name, capacity, location);
            int newId = hallDAO.insert(hall);
            sendData(resp, "{\"id\": " + newId + "}");

        } catch (NumberFormatException e) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Capacity must be a number.");
        } catch (IllegalArgumentException e) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (SQLException e) {
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error.");
        }
    }

    /** PUT — updates a hall. ADMIN only. Parameters: id, name, capacity, location */
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (!requireAdmin(req, resp)) return;

        try {
            int id          = Integer.parseInt(req.getParameter("id"));
            String name     = req.getParameter("name");
            int capacity    = Integer.parseInt(req.getParameter("capacity"));
            String location = req.getParameter("location");

            Hall hall = new Hall(id, name, capacity, location);
            hallDAO.update(hall);
            sendSuccess(resp, "Hall updated successfully.");

        } catch (NumberFormatException e) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid ID or capacity.");
        } catch (SQLException e) {
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error.");
        }
    }

    /** DELETE — deletes a hall. ADMIN only. Parameter: id */
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (!requireAdmin(req, resp)) return;

        try {
            int id = Integer.parseInt(req.getParameter("id"));
            hallDAO.delete(id);
            sendSuccess(resp, "Hall deleted successfully.");

        } catch (NumberFormatException e) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid hall ID.");
        } catch (SQLException e) {
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "Cannot delete hall — it may still have events assigned to it.");
        }
    }

    private String hallToJson(Hall h) {
        return "{\"id\": " + h.getId() +
               ", \"name\": \"" + escapeJson(h.getName()) + "\"" +
               ", \"capacity\": " + h.getCapacity() +
               ", \"location\": \"" + escapeJson(h.getLocation()) + "\"}";
    }
}
