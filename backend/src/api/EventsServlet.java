package api;

import model.Event;
import model.User;
import service.EventService;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * API Servlet for managing events.
 *
 * GET    /api/events         — list all events (any logged-in user)
 * GET    /api/events?id=X   — get one event by ID (any logged-in user)
 * POST   /api/events         — create new event (ADMIN only)
 * PUT    /api/events?id=X   — update event (ADMIN only)
 * DELETE /api/events?id=X   — delete event (ADMIN only)
 */
@WebServlet("/api/events")
public class EventsServlet extends BaseServlet {

    private final EventService eventService = new EventService();

    /**
     * GET — returns all events, or a single event if ?id= is provided.
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (!requireLogin(req, resp)) return;

        String idParam = req.getParameter("id");

        try {
            if (idParam != null) {
                // Return single event
                int id = Integer.parseInt(idParam);
                Event event = eventService.getEventById(id);
                sendData(resp, eventToJson(event));
            } else {
                // Return all events
                List<Event> events = eventService.getAllEvents();
                StringBuilder sb = new StringBuilder("[");
                for (int i = 0; i < events.size(); i++) {
                    sb.append(eventToJson(events.get(i)));
                    if (i < events.size() - 1) sb.append(",");
                }
                sb.append("]");
                sendData(resp, sb.toString());
            }
        } catch (NumberFormatException e) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid event ID.");
        } catch (IllegalArgumentException e) {
            sendError(resp, HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (SQLException e) {
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error.");
        }
    }

    /**
     * POST — creates a new event. ADMIN only.
     *
     * Parameters: title, description, eventDate (YYYY-MM-DD), eventTime (HH:MM), hallId
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (!requireAdmin(req, resp)) return;

        try {
            User admin = getLoggedInUser(req);
            String title       = req.getParameter("title");
            String description = req.getParameter("description");
            String eventDate   = req.getParameter("eventDate");
            String eventTime   = req.getParameter("eventTime");
            int hallId         = Integer.parseInt(req.getParameter("hallId"));

            int newId = eventService.createEvent(title, description, eventDate, eventTime, hallId, admin.getId());
            sendData(resp, "{\"id\": " + newId + "}");

        } catch (NumberFormatException e) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid hall ID.");
        } catch (IllegalArgumentException e) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (SQLException e) {
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error.");
        }
    }

    /**
     * PUT — updates an existing event. ADMIN only.
     *
     * Parameters: id, title, description, eventDate, eventTime, hallId
     */
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (!requireAdmin(req, resp)) return;

        try {
            int id             = Integer.parseInt(req.getParameter("id"));
            String title       = req.getParameter("title");
            String description = req.getParameter("description");
            String eventDate   = req.getParameter("eventDate");
            String eventTime   = req.getParameter("eventTime");
            int hallId         = Integer.parseInt(req.getParameter("hallId"));

            eventService.updateEvent(id, title, description, eventDate, eventTime, hallId);
            sendSuccess(resp, "Event updated successfully.");

        } catch (NumberFormatException e) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid ID format.");
        } catch (IllegalArgumentException e) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (SQLException e) {
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error.");
        }
    }

    /**
     * DELETE — deletes an event by ID. ADMIN only.
     *
     * Parameter: id
     */
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (!requireAdmin(req, resp)) return;

        try {
            int id = Integer.parseInt(req.getParameter("id"));
            eventService.deleteEvent(id);
            sendSuccess(resp, "Event deleted successfully.");

        } catch (NumberFormatException e) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid event ID.");
        } catch (IllegalArgumentException e) {
            sendError(resp, HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (SQLException e) {
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error.");
        }
    }

    /**
     * Converts an Event object to a JSON string.
     */
    private String eventToJson(Event e) {
        return "{" +
            "\"id\": " + e.getId() + "," +
            "\"title\": \"" + escapeJson(e.getTitle()) + "\"," +
            "\"description\": \"" + escapeJson(e.getDescription()) + "\"," +
            "\"eventDate\": \"" + e.getEventDate() + "\"," +
            "\"eventTime\": \"" + e.getEventTime() + "\"," +
            "\"hallId\": " + e.getHallId() + "," +
            "\"hallName\": \"" + escapeJson(e.getHallName()) + "\"," +
            "\"hallCapacity\": " + e.getHallCapacity() + "," +
            "\"registrationCount\": " + e.getRegistrationCount() + "," +
            "\"hasSpots\": " + e.hasAvailableSpots() +
            "}";
    }
}
