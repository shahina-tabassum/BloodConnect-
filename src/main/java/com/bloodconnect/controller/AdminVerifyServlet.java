package com.bloodconnect.controller;

import com.bloodconnect.dao.RequestDAO;
import com.google.gson.Gson;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles admin actions on blood requests.
 * POST /admin/verify → verifies/approves a request or updates its status
 */
@WebServlet("/admin/verify")
public class AdminVerifyServlet extends HttpServlet {

    private final RequestDAO requestDAO = new RequestDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);
        Map<String, Object> result = new HashMap<>();

        if (session == null || session.getAttribute("userId") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            result.put("success", false);
            result.put("message", "Unauthorized. Please log in.");
            response.getWriter().write(gson.toJson(result));
            return;
        }

        String role = (String) session.getAttribute("role");
        int adminId = (int) session.getAttribute("userId");

        if (!"ADMIN".equals(role)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            result.put("success", false);
            result.put("message", "Forbidden. Insufficient permissions.");
            response.getWriter().write(gson.toJson(result));
            return;
        }

        String action = request.getParameter("action");
        String requestIdStr = request.getParameter("requestId");

        if (requestIdStr == null || requestIdStr.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            result.put("success", false);
            result.put("message", "Missing request ID.");
            response.getWriter().write(gson.toJson(result));
            return;
        }

        try {
            int requestId = Integer.parseInt(requestIdStr);

            if ("verify".equals(action)) {
                requestDAO.verifyRequest(requestId, adminId);
                result.put("success", true);
                result.put("message", "Blood request verified successfully. Contact details are now public for matching donors.");
            } else if ("updateStatus".equals(action)) {
                String status = request.getParameter("status");
                if ("OPEN".equals(status) || "MATCHED".equals(status) || "FULFILLED".equals(status) || "CLOSED".equals(status)) {
                    requestDAO.updateStatus(requestId, status);
                    result.put("success", true);
                    result.put("message", "Blood request status updated to " + status + ".");
                } else {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    result.put("success", false);
                    result.put("message", "Invalid status value.");
                }
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                result.put("success", false);
                result.put("message", "Invalid action.");
            }

        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            result.put("success", false);
            result.put("message", "Invalid request ID format.");
        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            result.put("success", false);
            result.put("message", "Database error occurred during operation.");
        }

        response.getWriter().write(gson.toJson(result));
    }
}

