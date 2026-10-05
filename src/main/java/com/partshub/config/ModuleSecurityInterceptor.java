package com.partshub.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.PrintWriter;
import java.util.List;
import java.util.Set;

/**
 * Server-Side Security Boundary Enforcement Interceptor.
 * 
 * Enforces the module-ownership access matrix on all write operations (POST, PUT, DELETE, PATCH).
 * GET / View endpoints remain open to all authenticated roles for read-only access.
 * Business Owner (ROLE_OWNER / BUSINESS_OWNER) has full CRUD access across all modules.
 */
@Component
public class ModuleSecurityInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String method = request.getMethod();

        // Read operations (GET, HEAD, OPTIONS) are allowed for all roles across all modules
        if ("GET".equalsIgnoreCase(method) || "HEAD".equalsIgnoreCase(method) || "OPTIONS".equalsIgnoreCase(method)) {
            return true;
        }

        String uri = request.getRequestURI();

        // Exclude authentication and settings endpoints
        if (uri.startsWith("/api/auth/") || uri.startsWith("/api/account/")) {
            return true;
        }

        // Extract role from request header, attribute, or parameter
        String roleHeader = request.getHeader("X-User-Role");
        if (roleHeader == null || roleHeader.trim().isEmpty()) {
            roleHeader = (String) request.getSession().getAttribute("userRole");
        }
        if (roleHeader == null) {
            roleHeader = request.getParameter("role");
        }

        String role = normalizeRole(roleHeader);

        // Business Owner has full unrestricted CRUD access across all modules
        if ("BUSINESS_OWNER".equals(role) || "ROLE_OWNER".equals(role)) {
            return true;
        }

        // Check module ownership write permissions
        boolean isAuthorized = false;
        String requiredRole = "";

        if (uri.startsWith("/api/inventory")) {
            requiredRole = "STOREKEEPER";
            isAuthorized = "STOREKEEPER".equals(role);
        } else if (uri.startsWith("/api/suppliers")) {
            requiredRole = "PROCUREMENT_OFFICER";
            isAuthorized = "PROCUREMENT_OFFICER".equals(role);
        } else if (uri.startsWith("/api/purchases")) {
            requiredRole = "PURCHASING_AGENT";
            isAuthorized = "PURCHASING_AGENT".equals(role) || "STOREKEEPER".equals(role);
        } else if (uri.startsWith("/api/sales")) {
            requiredRole = "SALES_REP";
            isAuthorized = "SALES_REP".equals(role);
        } else if (uri.startsWith("/api/returns")) {
            requiredRole = "RETURNS_CLERK";
            isAuthorized = "RETURNS_CLERK".equals(role);
        } else if (uri.startsWith("/api/campaigns") || uri.startsWith("/api/reports")) {
            requiredRole = "BUSINESS_OWNER";
            isAuthorized = "BUSINESS_OWNER".equals(role);
        } else {
            // Default allow for unmapped utility APIs
            return true;
        }

        if (!isAuthorized) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json;charset=UTF-8");
            PrintWriter writer = response.getWriter();
            String userRoleText = role != null && !role.isEmpty() ? role : "UNAUTHENTICATED";
            writer.write(String.format(
                    "{\"error\":\"Access Denied: Role '%s' is not authorized to perform write operations on this module. Restricted to %s or BUSINESS_OWNER.\",\"status\":403}",
                    userRoleText, requiredRole
            ));
            writer.flush();
            return false;
        }

        return true;
    }

    private String normalizeRole(String rawRole) {
        if (rawRole == null) return "";
        String r = rawRole.trim().toUpperCase().replace(" ", "_");
        if (r.startsWith("ROLE_")) {
            r = r.substring(5);
        }
        if ("OWNER".equals(r) || "BUSINESS_OWNER".equals(r) || "ROLE_OWNER".equals(r)) return "BUSINESS_OWNER";
        if ("PURCHASING".equals(r) || "PURCHASING_AGENT".equals(r) || "ROLE_PURCHASING".equals(r) || "ROLE_PURCHASING_AGENT".equals(r)) return "PURCHASING_AGENT";
        if ("PROCUREMENT".equals(r) || "PROCUREMENT_OFFICER".equals(r) || "ROLE_PROCUREMENT".equals(r) || "ROLE_PROCUREMENT_OFFICER".equals(r)) return "PROCUREMENT_OFFICER";
        if ("SALES".equals(r) || "SALES_REP".equals(r) || "ROLE_SALES".equals(r) || "ROLE_SALES_REP".equals(r)) return "SALES_REP";
        if ("RETURNS".equals(r) || "RETURNS_CLERK".equals(r) || "ROLE_RETURNS".equals(r) || "ROLE_RETURNS_CLERK".equals(r)) return "RETURNS_CLERK";
        if ("STOREKEEPER".equals(r) || "ROLE_STOREKEEPER".equals(r)) return "STOREKEEPER";
        return r;
    }
}
