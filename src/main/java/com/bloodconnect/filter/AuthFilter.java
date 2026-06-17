package com.bloodconnect.filter;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Authentication and authorization filter.
 * Mapped to /* — intercepts every request.
 * 
 * - Allows public URLs (login, register, static assets) without session.
 * - Checks session.userId exists for all protected URLs.
 * - Enforces role-based access:
 *     /admin/*   → requires ADMIN
 *     /donor/*   → requires DONOR
 *     /request/* → requires REQUESTER
 */
@WebFilter("/*")
public class AuthFilter implements Filter {

    /** URLs that don't require authentication */
    private static final List<String> PUBLIC_URLS = Arrays.asList(
        "/login", "/register", "/logout", "/", "/index.html", "/login.html", "/register.html", "/error.html"
    );

    /** Static asset prefixes that don't require authentication */
    private static final List<String> STATIC_PREFIXES = Arrays.asList(
        "/css/", "/js/", "/images/", "/screenshots/", "/favicon.ico"
    );

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        String path = request.getRequestURI().substring(request.getContextPath().length());

        // Allow public URLs
        if (isPublicUrl(path)) {
            chain.doFilter(request, response);
            return;
        }

        // Allow static assets
        if (isStaticAsset(path)) {
            chain.doFilter(request, response);
            return;
        }

        // Check authentication
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            if (isApiRequest(path)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"Unauthorized. Please log in.\"}");
            } else {
                response.sendRedirect(request.getContextPath() + "/login.html");
            }
            return;
        }

        String role = (String) session.getAttribute("role");

        // Role-based authorization
        boolean authorized = true;
        if (path.startsWith("/admin/") || path.equals("/admin") || path.equals("/admin-dashboard.html")) {
            if (!"ADMIN".equals(role)) {
                authorized = false;
            }
        } else if (path.startsWith("/donor/") || path.equals("/donor") || path.equals("/donor-dashboard.html")) {
            if (!"DONOR".equals(role)) {
                authorized = false;
            }
        } else if (path.startsWith("/request/") || path.equals("/request") || path.equals("/requester-dashboard.html") 
                || path.equals("/request-form.html") || path.equals("/match-results.html") || path.equals("/match/find")) {
            if (!"REQUESTER".equals(role)) {
                authorized = false;
            }
        }

        if (!authorized) {
            if (isApiRequest(path)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"Forbidden. Insufficient permissions.\"}");
            } else {
                response.sendRedirect(request.getContextPath() + "/login.html?error=unauthorized");
            }
            return;
        }

        // Authenticated + authorized — proceed
        chain.doFilter(request, response);
    }

    private boolean isApiRequest(String path) {
        return path.equals("/donor/profile") 
            || path.startsWith("/request/") 
            || path.equals("/match/find") 
            || path.startsWith("/admin/")
            || path.equals("/login")
            || path.equals("/register")
            || path.equals("/logout");
    }

    private boolean isPublicUrl(String path) {
        for (String url : PUBLIC_URLS) {
            if (path.equals(url)) {
                return true;
            }
        }
        return false;
    }

    private boolean isStaticAsset(String path) {
        for (String prefix : STATIC_PREFIXES) {
            if (path.startsWith(prefix)) {
                return true;
            }
        }
        // Also allow root path (welcome file)
        return path.equals("/") || path.equals("");
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // No initialization needed
    }

    @Override
    public void destroy() {
        // No cleanup needed
    }
}
