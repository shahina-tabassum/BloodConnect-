package com.bloodconnect.controller;

import com.bloodconnect.dao.DonorDAO;
import com.bloodconnect.dao.UserDAO;
import com.bloodconnect.model.DonorProfile;
import com.bloodconnect.model.User;
import com.bloodconnect.util.CityList;
import com.bloodconnect.util.PasswordUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

/**
 * Handles user registration.
 * GET  /register → shows registration form
 * POST /register → validates input, creates user (+ donor profile if DONOR), redirects to login
 */
@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();
    private final DonorDAO donorDAO = new DonorDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Pass city list to JSP for dropdown
        request.setAttribute("cities", CityList.CITIES);
        request.getRequestDispatcher("/register.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Collect form fields
        String fullName = trim(request.getParameter("fullName"));
        String email = trim(request.getParameter("email"));
        String phone = trim(request.getParameter("phone"));
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");
        String role = trim(request.getParameter("role"));

        // Donor-specific fields
        String bloodGroup = trim(request.getParameter("bloodGroup"));
        String city = trim(request.getParameter("city"));

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
        // Server-side confirmPassword check — don't rely on JS alone
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

        // Check for validation errors
        if (errors.length() > 0) {
            request.setAttribute("error", errors.toString().trim());
            request.setAttribute("cities", CityList.CITIES);
            // Preserve form values
            request.setAttribute("formName", fullName);
            request.setAttribute("formEmail", email);
            request.setAttribute("formPhone", phone);
            request.setAttribute("formRole", role);
            request.setAttribute("formBloodGroup", bloodGroup);
            request.setAttribute("formCity", city);
            request.getRequestDispatcher("/register.jsp").forward(request, response);
            return;
        }

        try {
            // Check email uniqueness
            if (userDAO.findByEmail(email) != null) {
                request.setAttribute("error", "An account with this email already exists.");
                request.setAttribute("cities", CityList.CITIES);
                request.setAttribute("formName", fullName);
                request.setAttribute("formEmail", email);
                request.setAttribute("formPhone", phone);
                request.setAttribute("formRole", role);
                request.setAttribute("formBloodGroup", bloodGroup);
                request.setAttribute("formCity", city);
                request.getRequestDispatcher("/register.jsp").forward(request, response);
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

            // Success — redirect to login with success message
            response.sendRedirect(request.getContextPath() + "/login?registered=true");

        } catch (SQLException e) {
            e.printStackTrace();
            request.setAttribute("error", "Registration failed. Please try again.");
            request.setAttribute("cities", CityList.CITIES);
            request.getRequestDispatcher("/register.jsp").forward(request, response);
        }
    }

    private String trim(String s) {
        return s != null ? s.trim() : null;
    }

    private boolean isEmpty(String s) {
        return s == null || s.isEmpty();
    }
}
