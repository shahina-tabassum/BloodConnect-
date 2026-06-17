package com.bloodconnect.controller;

import com.bloodconnect.dao.UserDAO;
import com.bloodconnect.model.User;
import com.bloodconnect.util.PasswordUtil;
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
import java.util.Map;

/**
 * Handles user login.
 * GET  /login → checks session status
 * POST /login → authenticates, starts session, returns JSON role
 */
@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);
        Map<String, Object> result = new HashMap<>();

        if (session != null && session.getAttribute("userId") != null) {
            result.put("success", true);
            result.put("userId", session.getAttribute("userId"));
            result.put("role", session.getAttribute("role"));
            result.put("userName", session.getAttribute("userName"));
        } else {
            result.put("success", false);
        }

        response.getWriter().write(gson.toJson(result));
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String email = request.getParameter("email");
        String password = request.getParameter("password");
        Map<String, Object> result = new HashMap<>();

        if (email == null || email.trim().isEmpty() || password == null || password.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            result.put("success", false);
            result.put("message", "Email and password are required.");
            response.getWriter().write(gson.toJson(result));
            return;
        }

        try {
            User user = userDAO.findByEmail(email.trim());

            if (user == null || !PasswordUtil.checkPassword(password, user.getPasswordHash())) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                result.put("success", false);
                result.put("message", "Invalid email or password.");
                response.getWriter().write(gson.toJson(result));
                return;
            }

            // Authentication successful — create session
            HttpSession session = request.getSession(true);
            session.setAttribute("userId", user.getUserId());
            session.setAttribute("role", user.getRole());
            session.setAttribute("userName", user.getFullName());

            result.put("success", true);
            result.put("role", user.getRole());
            result.put("userName", user.getFullName());
            response.getWriter().write(gson.toJson(result));

        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            result.put("success", false);
            result.put("message", "Login failed. Database error occurred.");
            response.getWriter().write(gson.toJson(result));
        }
    }
}
