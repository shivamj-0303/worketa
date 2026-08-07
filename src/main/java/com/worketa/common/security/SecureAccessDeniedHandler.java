package com.worketa.common.security;

import java.io.IOException;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.worketa.common.response.ApiResponse;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Secure access denied handler.
 * Returns JSON error response without exposing stack traces or sensitive info.
 * CRITICAL: Don't leak what resources exist (avoid "Resource not found" vs "Access denied" distinction).
 */
@Component
public class SecureAccessDeniedHandler implements AccessDeniedHandler {

    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException) throws IOException, ServletException {
        
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        
        // Return generic response - don't reveal what permission was denied
        var apiResponse = ApiResponse.error("Forbidden", 
            "You do not have permission to access this resource.");
        
        response.getWriter().write(mapper.writeValueAsString(apiResponse));
    }
}
