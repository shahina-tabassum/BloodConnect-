package com.bloodconnect.controller;

import com.bloodconnect.dao.UserDAO;
import com.bloodconnect.model.User;
import com.bloodconnect.util.PasswordUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;

/**
 * Handles user login.
 * GET  /login → shows login form
 * POST /login → authenticates against DB via BCrypt, starts session, redirects by role
 */
@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // If already logged in, redirect to dashboard
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("userId") != null) {
            redirectByRole(request, response, (String) session.getAttribute("role"));
            return;
        }

        // Show success message if just registered
        if ("true".equals(request.getParameter("registered"))) {
            request.setAttribute("success", "Registration successful! Please log in.");
        }

        request.getRequestDispatcher("/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String email = request.getParameter("email");
        String password = request.getParameter("password");

        if (email == null || email.trim().isEmpty() || password == null || password.isEmpty()) {
            request.setAttribute("error", "Email and password are required.");
            request.getRequestDispatcher("/login.jsp").forward(request, response);
            return;
        }

        try {
            User user = userDAO.findByEmail(email.trim());

            if (user == null || !PasswordUtil.checkPassword(password, user.getPasswordHash())) {
                request.setAttribute("error", "Invalid email or password.");
                request.setAttribute("formEmail", email.trim());
                request.getRequestDispatcher("/login.jsp").forward(request, response);
                return;
            }

            // Authentication successful — create session
            HttpSession session = request.getSession(true);
            session.setAttribute("userId", user.getUserId());
            session.setAttribute("role", user.getRole());
            session.setAttribute("userName", user.getFullName());

            // Redirect based on role
            redirectByRole(request, response, user.getRole());

        } catch (SQLException e) {
            e.printStackTrace();
            request.setAttribute("error", "Login failed. Please try again.");
            request.getRequestDispatcher("/login.jsp").forward(request, response);
        }
    }

    private void redirectByRole(HttpServletRequest request, HttpServletResponse response, String role)
            throws IOException {
        String contextPath = request.getContextPath();
        switch (role) {
            case "DONOR":
                response.sendRedirect(contextPath + "/donor/profile");
                break;
            case "REQUESTER":
                response.sendRedirect(contextPath + "/request/list");
                break;
            case "ADMIN":
                response.sendRedirect(contextPath + "/admin/dashboard");
                break;
            default:
                response.sendRedirect(contextPath + "/login");
                break;
        }
    }
}
