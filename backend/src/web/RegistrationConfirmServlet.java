package web;

import model.Registration;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

/**
 * Shows the registration confirmation page after a successful registration.
 * Displays the event name, participant name, and QR token.
 *
 * URL: GET /registration-confirm
 */
@WebServlet("/registration-confirm")
public class RegistrationConfirmServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        Registration reg = (Registration) session.getAttribute("lastRegistration");
        if (reg == null) {
            resp.sendRedirect(req.getContextPath() + "/home");
            return;
        }

        // Remove from session so refreshing the page redirects to home
        session.removeAttribute("lastRegistration");

        req.setAttribute("registration", reg);
        req.getRequestDispatcher("/WEB-INF/views/registration-confirm.jsp").forward(req, resp);
    }
}
