package web;

import model.User;
import service.EventService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;

/**
 * Shows the main event listing page for regular users.
 *
 * GET /home — displays all events (home.jsp)
 */
@WebServlet("/home")
public class HomeServlet extends HttpServlet {

    private final EventService eventService = new EventService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // Check login
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        try {
            // Load all events and pass to JSP
            req.setAttribute("events", eventService.getAllEvents());
            req.setAttribute("user", (User) session.getAttribute("user"));
            req.getRequestDispatcher("/WEB-INF/views/home.jsp").forward(req, resp);

        } catch (SQLException e) {
            req.setAttribute("error", "Could not load events.");
            req.getRequestDispatcher("/WEB-INF/views/home.jsp").forward(req, resp);
        }
    }
}
