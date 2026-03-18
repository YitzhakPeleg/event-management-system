package api;

import db.ParticipantDAO;
import model.Participant;
import model.Registration;
import model.User;
import service.RegistrationService;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * API Servlet for managing event registrations.
 *
 * GET    /api/registrations?eventId=X         — list registrations for an event (ADMIN)
 * GET    /api/registrations?mine=true         — list my own registrations (USER)
 * GET    /api/registrations?token=XYZ         — validate a QR token (ADMIN, for entrance)
 * POST   /api/registrations                   — register to an event (USER)
 * DELETE /api/registrations?id=X             — cancel a registration (USER/ADMIN)
 */
@WebServlet("/api/registrations")
public class RegistrationsServlet extends BaseServlet {

    private final RegistrationService registrationService = new RegistrationService();
    private final ParticipantDAO      participantDAO      = new ParticipantDAO();

    /** GET — list registrations or validate a QR token. */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (!requireLogin(req, resp)) return;

        String eventIdParam = req.getParameter("eventId");
        String mine         = req.getParameter("mine");
        String token        = req.getParameter("token");

        try {
            if (token != null) {
                // QR code validation — ADMIN only
                if (!requireAdmin(req, resp)) return;
                Registration reg = registrationService.validateQRToken(token);
                sendData(resp, registrationToJson(reg));

            } else if ("true".equals(mine)) {
                // Return current user's own registrations
                User user = getLoggedInUser(req);
                Participant participant = participantDAO.findByUserId(user.getId());
                if (participant == null) {
                    sendData(resp, "[]");
                    return;
                }
                List<Registration> regs = registrationService.getRegistrationsForParticipant(participant.getId());
                sendData(resp, listToJson(regs));

            } else if (eventIdParam != null) {
                // List registrations for a specific event — ADMIN only
                if (!requireAdmin(req, resp)) return;
                int eventId = Integer.parseInt(eventIdParam);
                List<Registration> regs = registrationService.getRegistrationsForEvent(eventId);
                sendData(resp, listToJson(regs));

            } else {
                sendError(resp, HttpServletResponse.SC_BAD_REQUEST,
                    "Please provide eventId, mine=true, or token parameter.");
            }

        } catch (NumberFormatException e) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid event ID.");
        } catch (IllegalArgumentException e) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (SQLException e) {
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error.");
        }
    }

    /**
     * POST — register the current user to an event.
     *
     * Parameter: eventId
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (!requireLogin(req, resp)) return;

        try {
            int eventId = Integer.parseInt(req.getParameter("eventId"));
            User user = getLoggedInUser(req);

            // Get the participant profile for this user
            Participant participant = participantDAO.findByUserId(user.getId());
            if (participant == null) {
                sendError(resp, HttpServletResponse.SC_BAD_REQUEST,
                    "Please complete your participant profile before registering.");
                return;
            }

            Registration reg = registrationService.register(eventId, participant.getId());
            sendData(resp, registrationToJson(reg));

        } catch (NumberFormatException e) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid event ID.");
        } catch (IllegalArgumentException e) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (SQLException e) {
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error.");
        }
    }

    /**
     * DELETE — cancel a registration by its ID.
     *
     * Parameter: id
     */
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (!requireLogin(req, resp)) return;

        try {
            int id = Integer.parseInt(req.getParameter("id"));
            registrationService.cancelRegistration(id);
            sendSuccess(resp, "Registration cancelled.");

        } catch (NumberFormatException e) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid registration ID.");
        } catch (SQLException e) {
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error.");
        }
    }

    private String registrationToJson(Registration r) {
        return "{" +
            "\"id\": " + r.getId() + "," +
            "\"eventId\": " + r.getEventId() + "," +
            "\"eventTitle\": \"" + escapeJson(r.getEventTitle()) + "\"," +
            "\"eventDate\": \"" + r.getEventDate() + "\"," +
            "\"participantId\": " + r.getParticipantId() + "," +
            "\"participantName\": \"" + escapeJson(r.getParticipantName()) + "\"," +
            "\"registeredAt\": \"" + r.getRegisteredAt() + "\"," +
            "\"qrCodeToken\": \"" + escapeJson(r.getQrCodeToken()) + "\"" +
            "}";
    }

    private String listToJson(List<Registration> list) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            sb.append(registrationToJson(list.get(i)));
            if (i < list.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }
}
