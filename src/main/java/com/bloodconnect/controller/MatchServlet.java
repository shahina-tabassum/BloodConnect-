package com.bloodconnect.controller;

import com.bloodconnect.dao.MatchDAO;
import com.bloodconnect.dao.RequestDAO;
import com.bloodconnect.model.BloodRequest;
import com.bloodconnect.model.DonorMatch;

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
 * Handles retrieving matching donors for a blood request.
 * GET /match/find?requestId=X → lists matches, masking phone numbers if request is not admin-verified.
 */
@WebServlet("/match/find")
public class MatchServlet extends HttpServlet {

    private final RequestDAO requestDAO = new RequestDAO();
    private final MatchDAO matchDAO = new MatchDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        int userId = (int) session.getAttribute("userId");
        String role = (String) session.getAttribute("role");

        String requestIdStr = request.getParameter("requestId");
        if (requestIdStr == null || requestIdStr.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/request/list");
            return;
        }

        try {
            int requestId = Integer.parseInt(requestIdStr);
            BloodRequest bloodRequest = requestDAO.getRequestById(requestId);

            if (bloodRequest == null) {
                session.setAttribute("errorMsg", "Blood request not found.");
                response.sendRedirect(request.getContextPath() + "/request/list");
                return;
            }

            // Enforce authorization: Only the requester who posted it or an ADMIN can view the matches
            if (!"ADMIN".equals(role) && bloodRequest.getRequesterId() != userId) {
                response.sendRedirect(request.getContextPath() + "/login?error=unauthorized");
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

            request.setAttribute("bloodRequest", bloodRequest);
            request.setAttribute("matches", matches);

            request.getRequestDispatcher("/match-results.jsp").forward(request, response);

        } catch (NumberFormatException e) {
            session.setAttribute("errorMsg", "Invalid request ID format.");
            response.sendRedirect(request.getContextPath() + "/request/list");
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/error.jsp");
        }
    }
}
