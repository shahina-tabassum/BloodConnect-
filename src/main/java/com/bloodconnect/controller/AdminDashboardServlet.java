package com.bloodconnect.controller;

import com.bloodconnect.dao.RequestDAO;
import com.bloodconnect.dao.UserDAO;
import com.bloodconnect.dao.MatchDAO;
import com.bloodconnect.model.BloodRequest;
import com.bloodconnect.model.User;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles admin dashboard.
 * GET /admin/dashboard → returns list of requests, matched donor statuses, stats, and users in JSON.
 */
@WebServlet("/admin/dashboard")
public class AdminDashboardServlet extends HttpServlet {

    private final RequestDAO requestDAO = new RequestDAO();
    private final UserDAO userDAO = new UserDAO();
    private final MatchDAO matchDAO = new MatchDAO();
    private final Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\": \"Unauthorized\"}");
            return;
        }

        String role = (String) session.getAttribute("role");
        if (!"ADMIN".equals(role)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("{\"error\": \"Forbidden\"}");
            return;
        }

        Map<String, Object> result = new HashMap<>();

        try {
            List<BloodRequest> requests = requestDAO.getAllRequests();
            int pendingVerifications = 0;
            for (BloodRequest req : requests) {
                req.setMatches(matchDAO.getMatchesByRequest(req.getRequestId()));
                if (!req.isVerified()) {
                    pendingVerifications++;
                }
            }
            List<User> users = userDAO.getAllUsers();

            Map<String, Object> stats = new HashMap<>();
            stats.put("totalAccounts", users.size());
            stats.put("activeRequests", requests.size());
            stats.put("pendingVerifications", pendingVerifications);

            result.put("success", true);
            result.put("stats", stats);
            result.put("requests", requests);
            result.put("users", users);

            response.getWriter().write(gson.toJson(result));

        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            result.put("success", false);
            result.put("message", "Database error occurred while loading dashboard data.");
            response.getWriter().write(gson.toJson(result));
        }
    }
}
