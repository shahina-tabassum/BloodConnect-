package com.bloodconnect.controller;

import com.bloodconnect.dao.MatchDAO;
import com.bloodconnect.dao.RequestDAO;
import com.bloodconnect.model.BloodRequest;

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
 * Handles listing of requests for the logged-in requester.
 * GET /request/list → loads requests and their match counts, forwards to requester dashboard
 */
@WebServlet("/request/list")
public class RequestListServlet extends HttpServlet {

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

        int requesterId = (int) session.getAttribute("userId");

        try {
            List<BloodRequest> requests = requestDAO.getRequestsByRequester(requesterId);
            Map<Integer, Integer> matchCounts = new HashMap<>();

            for (BloodRequest req : requests) {
                // Get all matches for this request to count them
                int count = matchDAO.getMatchesByRequest(req.getRequestId()).size();
                matchCounts.put(req.getRequestId(), count);
            }

            request.setAttribute("requests", requests);
            request.setAttribute("matchCounts", matchCounts);

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

            request.getRequestDispatcher("/requester-dashboard.jsp").forward(request, response);

        } catch (SQLException e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/error.jsp");
        }
    }
}
