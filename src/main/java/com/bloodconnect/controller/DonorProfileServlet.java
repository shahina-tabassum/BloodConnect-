package com.bloodconnect.controller;

import com.bloodconnect.dao.DonorDAO;
import com.bloodconnect.dao.UserDAO;
import com.bloodconnect.dao.MatchDAO;
import com.bloodconnect.model.DonorProfile;
import com.bloodconnect.model.User;
import com.bloodconnect.model.DonorMatch;
import com.bloodconnect.util.CityList;

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
import java.util.List;

/**
 * Handles donor profile view and updates.
 * GET  /donor/profile → shows donor dashboard with profile edit form and matching history
 * POST /donor/profile → updates donor profile and toggles availability
 */
@WebServlet("/donor/profile")
public class DonorProfileServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();
    private final DonorDAO donorDAO = new DonorDAO();
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

        try {
            User user = userDAO.findById(userId);
            DonorProfile profile = donorDAO.getProfile(userId);
            List<DonorMatch> matches = matchDAO.getMatchesByDonor(userId);

            if (user == null) {
                response.sendRedirect(request.getContextPath() + "/logout");
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

            request.setAttribute("user", user);
            request.setAttribute("profile", profile);
            request.setAttribute("matches", matches);
            request.setAttribute("cities", CityList.CITIES);
            request.setAttribute("daysSinceLastDonation", daysSinceLastDonation);
            request.setAttribute("isEligible", isEligible);

            // Fetch flash message if any
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

            request.getRequestDispatcher("/donor-dashboard.jsp").forward(request, response);

        } catch (SQLException e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/error.jsp");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        int userId = (int) session.getAttribute("userId");
        String action = request.getParameter("action");

        try {
            if ("toggleAvailability".equals(action)) {
                // Quick availability toggle action
                boolean isAvailable = Boolean.parseBoolean(request.getParameter("isAvailable"));
                donorDAO.toggleAvailability(userId, isAvailable);
                session.setAttribute("successMsg", "Availability updated successfully.");
                response.sendRedirect(request.getContextPath() + "/donor/profile");
                return;
            }

            if ("updateMatchStatus".equals(action)) {
                // Quick match status update (Accept/Decline)
                int matchId = Integer.parseInt(request.getParameter("matchId"));
                String status = request.getParameter("status");
                if ("ACCEPTED".equals(status) || "DECLINED".equals(status)) {
                    matchDAO.updateMatchStatus(matchId, status);
                    session.setAttribute("successMsg", "Match request " + status.toLowerCase() + ".");
                } else {
                    session.setAttribute("errorMsg", "Invalid status update.");
                }
                response.sendRedirect(request.getContextPath() + "/donor/profile");
                return;
            }

            // Retrieve form inputs for full profile update
            String bloodGroup = trim(request.getParameter("bloodGroup"));
            String ageStr = trim(request.getParameter("age"));
            String gender = trim(request.getParameter("gender"));
            String city = trim(request.getParameter("city"));
            String pincode = trim(request.getParameter("pincode"));
            String lastDonationDateStr = trim(request.getParameter("lastDonationDate"));
            boolean isAvailable = request.getParameter("isAvailable") != null; // checkbox checked

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

                    // Check that the donation date is not in the future
                    if (lastDonationDate.after(new java.util.Date())) {
                        errors.append("Last donation date cannot be in the future. ");
                    }
                } catch (ParseException e) {
                    errors.append("Invalid date format. ");
                }
            }

            if (errors.length() > 0) {
                session.setAttribute("errorMsg", errors.toString().trim());
                response.sendRedirect(request.getContextPath() + "/donor/profile");
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

            session.setAttribute("successMsg", "Profile updated successfully.");
            response.sendRedirect(request.getContextPath() + "/donor/profile");

        } catch (SQLException e) {
            e.printStackTrace();
            session.setAttribute("errorMsg", "Failed to update profile due to database error.");
            response.sendRedirect(request.getContextPath() + "/donor/profile");
        }
    }

    private String trim(String s) {
        return s != null ? s.trim() : null;
    }

    private boolean isEmpty(String s) {
        return s == null || s.isEmpty();
    }
}
