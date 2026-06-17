package com.bloodconnect.controller;

import com.bloodconnect.dao.DonorDAO;
import com.bloodconnect.dao.UserDAO;
import com.bloodconnect.dao.MatchDAO;
import com.bloodconnect.model.DonorProfile;
import com.bloodconnect.model.User;
import com.bloodconnect.model.DonorMatch;
import com.bloodconnect.util.CityList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles donor profile view and updates.
 * GET  /donor/profile → returns donor details, eligibility, matches history in JSON
 * POST /donor/profile → updates donor profile, toggles availability, or updates match status
 */
@WebServlet("/donor/profile")
public class DonorProfileServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();
    private final DonorDAO donorDAO = new DonorDAO();
    private final MatchDAO matchDAO = new MatchDAO();
    private final Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();

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
        Map<String, Object> result = new HashMap<>();

        try {
            User user = userDAO.findById(userId);
            DonorProfile profile = donorDAO.getProfile(userId);
            List<DonorMatch> matches = matchDAO.getMatchesByDonor(userId);

            if (user == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                result.put("success", false);
                result.put("message", "User not found");
                response.getWriter().write(gson.toJson(result));
                return;
            }

            // If profile does not exist for some reason, create a default one
            if (profile == null) {
                profile = new DonorProfile();
                profile.setDonorId(userId);
                profile.setBloodGroup("O+");
                profile.setCity("Mumbai");
                profile.setAvailable(true);
                donorDAO.createProfile(profile);
            }

            // Compute eligibility (90-day donation gap)
            long daysSinceLastDonation = -1;
            boolean isEligible = true;
            if (profile.getLastDonationDate() != null) {
                long diffMs = System.currentTimeMillis() - profile.getLastDonationDate().getTime();
                daysSinceLastDonation = diffMs / (1000L * 60 * 60 * 24);
                if (daysSinceLastDonation < 0) daysSinceLastDonation = 0;
                isEligible = daysSinceLastDonation > 90;
            }

            result.put("success", true);
            result.put("user", user);
            result.put("profile", profile);
            result.put("matches", matches);
            result.put("cities", CityList.CITIES);
            result.put("daysSinceLastDonation", daysSinceLastDonation);
            result.put("isEligible", isEligible);

            response.getWriter().write(gson.toJson(result));

        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            result.put("success", false);
            result.put("message", "Database error occurred while loading profile.");
            response.getWriter().write(gson.toJson(result));
        }
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

        int userId = (int) session.getAttribute("userId");
        String action = request.getParameter("action");
        Map<String, Object> result = new HashMap<>();

        try {
            if ("toggleAvailability".equals(action)) {
                boolean isAvailable = Boolean.parseBoolean(request.getParameter("isAvailable"));
                donorDAO.toggleAvailability(userId, isAvailable);
                result.put("success", true);
                result.put("message", "Availability updated successfully.");
                response.getWriter().write(gson.toJson(result));
                return;
            }

            if ("updateMatchStatus".equals(action)) {
                int matchId = Integer.parseInt(request.getParameter("matchId"));
                String status = request.getParameter("status");
                if ("ACCEPTED".equals(status) || "DECLINED".equals(status)) {
                    matchDAO.updateMatchStatus(matchId, status);
                    result.put("success", true);
                    result.put("message", "Match request " + status.toLowerCase() + ".");
                } else {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    result.put("success", false);
                    result.put("message", "Invalid status update.");
                }
                response.getWriter().write(gson.toJson(result));
                return;
            }

            // Retrieve form inputs for full profile update
            String bloodGroup = trim(request.getParameter("bloodGroup"));
            String ageStr = trim(request.getParameter("age"));
            String gender = trim(request.getParameter("gender"));
            String city = trim(request.getParameter("city"));
            String pincode = trim(request.getParameter("pincode"));
            String lastDonationDateStr = trim(request.getParameter("lastDonationDate"));
            boolean isAvailable = request.getParameter("isAvailable") != null && Boolean.parseBoolean(request.getParameter("isAvailable"));

            StringBuilder errors = new StringBuilder();

            if (isEmpty(bloodGroup)) {
                errors.append("Blood group is required. ");
            }
            if (isEmpty(city)) {
                errors.append("City is required. ");
            }
            if (!isEmpty(pincode) && !pincode.matches("^[0-9]{6}$")) {
                errors.append("Pincode must be exactly 6 digits. ");
            }

            Integer age = null;
            if (!isEmpty(ageStr)) {
                try {
                    age = Integer.parseInt(ageStr);
                    if (age < 18 || age > 65) {
                        errors.append("Donor age must be between 18 and 65. ");
                    }
                } catch (NumberFormatException e) {
                    errors.append("Age must be a valid number. ");
                }
            }

            Date lastDonationDate = null;
            if (!isEmpty(lastDonationDateStr)) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    sdf.setLenient(false);
                    java.util.Date parsedDate = sdf.parse(lastDonationDateStr);
                    lastDonationDate = new Date(parsedDate.getTime());

                    if (lastDonationDate.after(new java.util.Date())) {
                        errors.append("Last donation date cannot be in the future. ");
                    }
                } catch (ParseException e) {
                    errors.append("Invalid date format. ");
                }
            }

            if (errors.length() > 0) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                result.put("success", false);
                result.put("message", errors.toString().trim());
                response.getWriter().write(gson.toJson(result));
                return;
            }

            // Construct and update profile
            DonorProfile dp = new DonorProfile();
            dp.setDonorId(userId);
            dp.setBloodGroup(bloodGroup);
            dp.setAge(age);
            dp.setGender(gender);
            dp.setCity(city);
            dp.setPincode(pincode);
            dp.setLastDonationDate(lastDonationDate);
            dp.setAvailable(isAvailable);

            donorDAO.updateProfile(dp);

            result.put("success", true);
            result.put("message", "Profile updated successfully.");
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
