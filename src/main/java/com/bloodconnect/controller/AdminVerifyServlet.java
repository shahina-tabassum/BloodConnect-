package com.bloodconnect.controller;

import com.bloodconnect.dao.RequestDAO;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;

/**
 * Handles admin actions on blood requests.
 * POST /admin/verify → verifies/approves a request or updates its status
 */
@WebServlet("/admin/verify")
public class AdminVerifyServlet extends HttpServlet {

    private final RequestDAO requestDAO = new RequestDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String role = (String) session.getAttribute("role");
        int adminId = (int) session.getAttribute("userId");

        if (!"ADMIN".equals(role)) {
            response.sendRedirect(request.getContextPath() + "/login?error=unauthorized");
            return;
        }

        String action = request.getParameter("action");
        String requestIdStr = request.getParameter("requestId");

        if (requestIdStr == null || requestIdStr.isEmpty()) {
            session.setAttribute("errorMsg", "Missing request ID.");
            response.sendRedirect(request.getContextPath() + "/admin/dashboard");
            return;
        }

        try {
            int requestId = Integer.parseInt(requestIdStr);

            if ("verify".equals(action)) {
                requestDAO.verifyRequest(requestId, adminId);
                // Also update status to MATCHED if matches exist, or keep OPEN
                session.setAttribute("successMsg", "Blood request verified successfully. Contact details are now public for matching donors.");
            } else if ("updateStatus".equals(action)) {
                String status = request.getParameter("status");
                if ("OPEN".equals(status) || "MATCHED".equals(status) || "FULFILLED".equals(status) || "CLOSED".equals(status)) {
                    requestDAO.updateStatus(requestId, status);
                    session.setAttribute("successMsg", "Blood request status updated to " + status + ".");
                } else {
                    session.setAttribute("errorMsg", "Invalid status value.");
                }
            } else {
                session.setAttribute("errorMsg", "Invalid action.");
            }

        } catch (NumberFormatException e) {
            session.setAttribute("errorMsg", "Invalid request ID format.");
        } catch (SQLException e) {
            e.printStackTrace();
            session.setAttribute("errorMsg", "Database error occurred during operation.");
        }

        response.sendRedirect(request.getContextPath() + "/admin/dashboard");
    }
}
