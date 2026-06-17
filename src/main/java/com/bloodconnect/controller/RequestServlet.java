package com.bloodconnect.controller;

import com.bloodconnect.dao.DonorDAO;
import com.bloodconnect.dao.MatchDAO;
import com.bloodconnect.dao.RequestDAO;
import com.bloodconnect.model.BloodRequest;
import com.bloodconnect.model.DonorProfile;
import com.bloodconnect.util.CityList;
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
 * Handles creation of new blood requests.
 * GET  /request/new → returns Indian cities dropdown list in JSON
 * POST /request/new → validates, saves request, auto-matches available donors, and returns JSON stats
 */
@WebServlet("/request/new")
public class RequestServlet extends HttpServlet {

    private final RequestDAO requestDAO = new RequestDAO();
    private final DonorDAO donorDAO = new DonorDAO();
    private final MatchDAO matchDAO = new MatchDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(gson.toJson(CityList.CITIES));
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
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

        // Retrieve inputs
        String patientName = trim(request.getParameter("patientName"));
        String bloodGroupNeeded = trim(request.getParameter("bloodGroupNeeded"));
        String unitsStr = trim(request.getParameter("unitsRequired"));
        String hospitalName = trim(request.getParameter("hospitalName"));
        String city = trim(request.getParameter("city"));
        String urgency = trim(request.getParameter("urgency"));

        Map<String, Object> result = new HashMap<>();
        StringBuilder errors = new StringBuilder();

        if (isEmpty(patientName)) {
            errors.append("Patient name is required. ");
        }
        if (isEmpty(bloodGroupNeeded)) {
            errors.append("Blood group needed is required. ");
        }
        if (isEmpty(hospitalName)) {
            errors.append("Hospital name is required. ");
        }
        if (isEmpty(city)) {
            errors.append("City is required. ");
        }
        if (isEmpty(urgency)) {
            errors.append("Urgency level is required. ");
        }

        int unitsRequired = 0;
        if (isEmpty(unitsStr)) {
            errors.append("Units required is required. ");
        } else {
            try {
                unitsRequired = Integer.parseInt(unitsStr);
                if (unitsRequired <= 0) {
                    errors.append("Units required must be a positive number. ");
                }
            } catch (NumberFormatException e) {
                errors.append("Units required must be a valid number. ");
            }
        }

        if (errors.length() > 0) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            result.put("success", false);
            result.put("message", errors.toString().trim());
            response.getWriter().write(gson.toJson(result));
            return;
        }

        try {
            // Create the BloodRequest
            BloodRequest req = new BloodRequest();
            req.setRequesterId(requesterId);
            req.setPatientName(patientName);
            req.setBloodGroupNeeded(bloodGroupNeeded);
            req.setUnitsRequired(unitsRequired);
            req.setHospitalName(hospitalName);
            req.setCity(city);
            req.setUrgency(urgency);
            req.setStatus("OPEN");
            req.setVerified(false);

            int requestId = requestDAO.createRequest(req);
            if (requestId <= 0) {
                throw new SQLException("Failed to save blood request.");
            }

            // Run Auto-Matching Engine
            List<DonorProfile> eligibleDonors = donorDAO.findEligibleDonors(bloodGroupNeeded, city);
            int matchCount = 0;
            for (DonorProfile donor : eligibleDonors) {
                if (!matchDAO.matchExists(requestId, donor.getDonorId())) {
                    matchDAO.createMatch(requestId, donor.getDonorId());
                    matchCount++;
                }
            }

            result.put("success", true);
            result.put("message", "Blood request posted successfully!");
            result.put("matchCount", matchCount);
            result.put("requestId", requestId);
            response.getWriter().write(gson.toJson(result));

        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            result.put("success", false);
            result.put("message", "Failed to update profile due to database error.");
            response.getWriter().write(gson.toJson(result));
        }
    }

    private String trim(String s) {
        return s != null ? s.trim() : null;
    }

    private boolean isEmpty(String s) {
        return s == null || s.isEmpty();
    }
}
