package com.bloodconnect.controller;

import com.bloodconnect.dao.MatchDAO;
import com.bloodconnect.dao.RequestDAO;
import com.bloodconnect.model.BloodRequest;
import com.google.gson.Gson;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles listing of requests for the logged-in requester.
 * GET /request/list → loads requests and their match counts, returns JSON
 */
@WebServlet("/request/list")
public class RequestListServlet extends HttpServlet {

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

        int requesterId = (int) session.getAttribute("userId");
        Map<String, Object> result = new HashMap<>();

        try {
            List<BloodRequest> requests = requestDAO.getRequestsByRequester(requesterId);
            List<Map<String, Object>> requestList = new ArrayList<>();

            for (BloodRequest req : requests) {
                Map<String, Object> reqMap = new HashMap<>();
                reqMap.put("requestId", req.getRequestId());
                reqMap.put("patientName", req.getPatientName());
                reqMap.put("bloodGroupNeeded", req.getBloodGroupNeeded());
                reqMap.put("unitsRequired", req.getUnitsRequired());
                reqMap.put("hospitalName", req.getHospitalName());
                reqMap.put("city", req.getCity());
                reqMap.put("urgency", req.getUrgency());
                reqMap.put("status", req.getStatus());
                reqMap.put("isVerified", req.isVerified());
                reqMap.put("createdAt", req.getCreatedAt() != null ? req.getCreatedAt().toString() : "");

                int count = matchDAO.getMatchesByRequest(req.getRequestId()).size();
                reqMap.put("matchCount", count);
                requestList.add(reqMap);
            }

            result.put("success", true);
            result.put("requests", requestList);
            response.getWriter().write(gson.toJson(result));

        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            result.put("success", false);
            result.put("message", "Database error occurred while retrieving request list.");
            response.getWriter().write(gson.toJson(result));
        }
    }
}
