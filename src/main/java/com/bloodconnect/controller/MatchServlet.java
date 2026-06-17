package com.bloodconnect.controller;

import com.bloodconnect.dao.MatchDAO;
import com.bloodconnect.dao.RequestDAO;
import com.bloodconnect.model.BloodRequest;
import com.bloodconnect.model.DonorMatch;
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
import java.util.List;
import java.util.Map;

/**
 * Handles retrieving matching donors for a blood request.
 * GET /match/find?requestId=X → lists matches, masking phone numbers if request is not admin-verified, returns JSON
 */
@WebServlet("/match/find")
public class MatchServlet extends HttpServlet {

    private final RequestDAO requestDAO = new RequestDAO();
    private final MatchDAO matchDAO = new MatchDAO();
    private final Gson gson = new Gson();

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

        int userId = (int) session.getAttribute("userId");
        String role = (String) session.getAttribute("role");

        String requestIdStr = request.getParameter("requestId");
        Map<String, Object> result = new HashMap<>();

        if (requestIdStr == null || requestIdStr.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            result.put("success", false);
            result.put("message", "Request ID is required.");
            response.getWriter().write(gson.toJson(result));
            return;
        }

        try {
            int requestId = Integer.parseInt(requestIdStr);
            BloodRequest bloodRequest = requestDAO.getRequestById(requestId);

            if (bloodRequest == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                result.put("success", false);
                result.put("message", "Blood request not found.");
                response.getWriter().write(gson.toJson(result));
                return;
            }

            // Enforce authorization: Only the requester who posted it or an ADMIN can view the matches
            if (!"ADMIN".equals(role) && bloodRequest.getRequesterId() != userId) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                result.put("success", false);
                result.put("message", "Forbidden. You are not authorized to view these matches.");
                response.getWriter().write(gson.toJson(result));
                return;
            }

            List<DonorMatch> matches = matchDAO.getMatchesByRequest(requestId);

            // Contact masking logic: done server-side in servlet
            for (DonorMatch match : matches) {
                if (!bloodRequest.isVerified()) {
                    String phone = match.getDonorPhone();
                    if (phone != null && phone.length() >= 4) {
                        // Mask middle digits: 98xxxxxx10
                        match.setDonorPhone(phone.substring(0, 2) + "******" + phone.substring(phone.length() - 2));
                    } else {
                        match.setDonorPhone("********");
                    }
                }
            }

            result.put("success", true);
            result.put("bloodRequest", bloodRequest);
            result.put("matches", matches);

            response.getWriter().write(gson.toJson(result));

        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            result.put("success", false);
            result.put("message", "Invalid request ID format.");
            response.getWriter().write(gson.toJson(result));
        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            result.put("success", false);
            result.put("message", "Database error occurred while loading matches.");
            response.getWriter().write(gson.toJson(result));
        }
    }
}
