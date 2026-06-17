package com.bloodconnect.controller;

import com.bloodconnect.dao.DonorDAO;
import com.bloodconnect.dao.UserDAO;
import com.bloodconnect.model.DonorProfile;
import com.bloodconnect.model.User;
import com.bloodconnect.util.CityList;
import com.bloodconnect.util.PasswordUtil;
import com.google.gson.Gson;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles user registration.
 * GET  /register → returns Indian cities dropdown list in JSON
 * POST /register → validates input, creates user (+ donor profile if DONOR), returns JSON status
 */
@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();
    private final DonorDAO donorDAO = new DonorDAO();
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

        // Collect fields
        String fullName = trim(request.getParameter("fullName"));
        String email = trim(request.getParameter("email"));
        String phone = trim(request.getParameter("phone"));
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");
        String role = trim(request.getParameter("role"));

        // Donor-specific fields
        String bloodGroup = trim(request.getParameter("bloodGroup"));
        String city = trim(request.getParameter("city"));

        Map<String, Object> result = new HashMap<>();

        // --- Server-side validation ---
        StringBuilder errors = new StringBuilder();

        if (isEmpty(fullName)) {
            errors.append("Full name is required. ");
        }
        if (isEmpty(email) || !email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            errors.append("Valid email is required. ");
        }
        if (isEmpty(phone) || !phone.matches("^[0-9]{10,15}$")) {
            errors.append("Valid phone number (10-15 digits) is required. ");
        }
        if (password == null || password.length() < 8) {
            errors.append("Password must be at least 8 characters. ");
        }
        if (password != null && !password.equals(confirmPassword)) {
            errors.append("Passwords do not match. ");
        }
        if (!"DONOR".equals(role) && !"REQUESTER".equals(role)) {
            errors.append("Invalid role selected. ");
        }
        if ("DONOR".equals(role)) {
            if (isEmpty(bloodGroup)) {
                errors.append("Blood group is required for donors. ");
            }
            if (isEmpty(city)) {
                errors.append("City is required for donors. ");
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
            // Check email uniqueness
            if (userDAO.findByEmail(email) != null) {
                response.setStatus(HttpServletResponse.SC_CONFLICT);
                result.put("success", false);
                result.put("message", "An account with this email already exists.");
                response.getWriter().write(gson.toJson(result));
                return;
            }

            // Hash password
            String hashedPassword = PasswordUtil.hashPassword(password);

            // Create user
            User user = new User(fullName, email, hashedPassword, phone, role);
            int userId = userDAO.register(user);

            if (userId <= 0) {
                throw new SQLException("Failed to create user account.");
            }

            // If donor, create donor profile
            if ("DONOR".equals(role)) {
                DonorProfile dp = new DonorProfile();
                dp.setDonorId(userId);
                dp.setBloodGroup(bloodGroup);
                dp.setCity(city);
                dp.setAvailable(true);
                donorDAO.createProfile(dp);
            }

            response.setStatus(HttpServletResponse.SC_CREATED);
            result.put("success", true);
            result.put("message", "Registration successful!");
            response.getWriter().write(gson.toJson(result));

        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            result.put("success", false);
            result.put("message", "Registration failed due to a database error.");
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
