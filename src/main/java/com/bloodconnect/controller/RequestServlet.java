package com.bloodconnect.controller;

import com.bloodconnect.dao.DonorDAO;
import com.bloodconnect.dao.MatchDAO;
import com.bloodconnect.dao.RequestDAO;
import com.bloodconnect.model.BloodRequest;
import com.bloodconnect.model.DonorProfile;
import com.bloodconnect.util.CityList;

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
 * Handles creation of new blood requests.
 * GET  /request/new → shows request form
 * POST /request/new → validates, saves request, auto-matches available donors, and redirects to list
 */
@WebServlet("/request/new")
public class RequestServlet extends HttpServlet {

    private final RequestDAO requestDAO = new RequestDAO();
    private final DonorDAO donorDAO = new DonorDAO();
    private final MatchDAO matchDAO = new MatchDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setAttribute("cities", CityList.CITIES);
        request.getRequestDispatcher("/request-form.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        int requesterId = (int) session.getAttribute("userId");

        // Retrieve form fields
        String patientName = trim(request.getParameter("patientName"));
        String bloodGroupNeeded = trim(request.getParameter("bloodGroupNeeded"));
        String unitsStr = trim(request.getParameter("unitsRequired"));
        String hospitalName = trim(request.getParameter("hospitalName"));
        String city = trim(request.getParameter("city"));
        String urgency = trim(request.getParameter("urgency"));

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
            request.setAttribute("error", errors.toString().trim());
            request.setAttribute("cities", CityList.CITIES);
            // Preserve form values
            request.setAttribute("formPatientName", patientName);
            request.setAttribute("formBloodGroup", bloodGroupNeeded);
            request.setAttribute("formUnits", unitsStr);
            request.setAttribute("formHospitalName", hospitalName);
            request.setAttribute("formCity", city);
            request.setAttribute("formUrgency", urgency);
            request.getRequestDispatcher("/request-form.jsp").forward(request, response);
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
                // Ensure no duplicate matches
                if (!matchDAO.matchExists(requestId, donor.getDonorId())) {
                    matchDAO.createMatch(requestId, donor.getDonorId());
                    matchCount++;
                }
            }

            // Store status in session flash message
            session.setAttribute("successMsg", "Blood request posted successfully! Found " + matchCount + " potential match(es). Pending admin verification.");
            response.sendRedirect(request.getContextPath() + "/request/list");

        } catch (SQLException e) {
            e.printStackTrace();
            request.setAttribute("error", "Database error occurred while processing request.");
            request.setAttribute("cities", CityList.CITIES);
            request.getRequestDispatcher("/request-form.jsp").forward(request, response);
        }
    }

    private String trim(String s) {
        return s != null ? s.trim() : null;
    }

    private boolean isEmpty(String s) {
        return s == null || s.isEmpty();
    }
}
