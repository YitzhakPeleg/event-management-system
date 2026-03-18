package web;

import model.User;
import service.EventService;
import db.HallDAO;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;

/**
 * Shows the admin dashboard and handles admin actions.
 *
 * GET /admin — displays admin dashboard with events and halls (admin/dashboard.jsp)
 */
@WebServlet("/admin")
public class AdminServlet extends HttpServlet {

    private final EventService eventService = new EventService();
    private final HallDAO      hallDAO      = new HallDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // Check login and admin role
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        User user = (User) session.getAttribute("user");
        if (!user.isAdmin()) {
            resp.sendRedirect(req.getContextPath() + "/home");
            return;
        }

        try {
            req.setAttribute("events", eventService.getAllEvents());
            req.setAttribute("halls", hallDAO.findAll());
            req.setAttribute("user", user);
            req.getRequestDispatcher("/WEB-INF/views/admin/dashboard.jsp").forward(req, resp);

        } catch (SQLException e) {
            req.setAttribute("error", "Could not load data.");
            req.getRequestDispatcher("/WEB-INF/views/admin/dashboard.jsp").forward(req, resp);
        }
    }
}
