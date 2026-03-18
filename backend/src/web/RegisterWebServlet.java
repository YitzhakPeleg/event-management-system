package web;

import db.ParticipantDAO;
import model.Participant;
import model.Registration;
import model.User;
import service.RegistrationService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;

/**
 * Handles event registration submitted from the web form (home.jsp).
 * On success: redirects to a confirmation page showing the QR token.
 * On failure: redirects back to home with an error message.
 *
 * URL: POST /register
 */
@WebServlet("/register")
public class RegisterWebServlet extends HttpServlet {

    private final RegistrationService registrationService = new RegistrationService();
    private final ParticipantDAO      participantDAO      = new ParticipantDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // Check login
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        User user = (User) session.getAttribute("user");

        try {
            int eventId = Integer.parseInt(req.getParameter("eventId"));

            // Get participant profile for this user
            Participant participant = participantDAO.findByUserId(user.getId());
            if (participant == null) {
                session.setAttribute("flashError", "Please complete your profile before registering.");
                resp.sendRedirect(req.getContextPath() + "/home");
                return;
            }

            Registration reg = registrationService.register(eventId, participant.getId());

            // Store registration in session and redirect to confirmation page
            session.setAttribute("lastRegistration", reg);
            resp.sendRedirect(req.getContextPath() + "/registration-confirm");

        } catch (NumberFormatException e) {
            session.setAttribute("flashError", "Invalid event.");
            resp.sendRedirect(req.getContextPath() + "/home");
        } catch (IllegalArgumentException e) {
            // Capacity full, already registered, etc.
            session.setAttribute("flashError", e.getMessage());
            resp.sendRedirect(req.getContextPath() + "/home");
        } catch (SQLException e) {
            session.setAttribute("flashError", "A database error occurred. Please try again.");
            resp.sendRedirect(req.getContextPath() + "/home");
        }
    }
}
