package com.bloodconnect.controller;

import com.bloodconnect.dao.RequestDAO;
import com.bloodconnect.dao.UserDAO;
import com.bloodconnect.dao.MatchDAO;
import com.bloodconnect.model.BloodRequest;
import com.bloodconnect.model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * Handles admin dashboard.
 * GET /admin/dashboard → displays list of requests (with verify actions) and list of registered users.
 */
@WebServlet("/admin/dashboard")
public class AdminDashboardServlet extends HttpServlet {

    private final RequestDAO requestDAO = new RequestDAO();
    private final UserDAO userDAO = new UserDAO();
    private final MatchDAO matchDAO = new MatchDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String role = (String) session.getAttribute("role");
        if (!"ADMIN".equals(role)) {
            response.sendRedirect(request.getContextPath() + "/login?error=unauthorized");
            return;
        }

        try {
            List<BloodRequest> requests = requestDAO.getAllRequests();
            for (BloodRequest req : requests) {
                req.setMatches(matchDAO.getMatchesByRequest(req.getRequestId()));
            }
            List<User> users = userDAO.getAllUsers();

            request.setAttribute("requests", requests);
            request.setAttribute("users", users);

            // Fetch flash messages if any
            String success = (String) session.getAttribute("successMsg");
            if (success != null) {
                request.setAttribute("success", success);
                session.removeAttribute("successMsg");
            }
            String error = (String) session.getAttribute("errorMsg");
            if (error != null) {
                request.setAttribute("error", error);
                session.removeAttribute("errorMsg");
            }

            request.getRequestDispatcher("/admin-dashboard.jsp").forward(request, response);

        } catch (SQLException e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/error.jsp");
        }
    }
}
