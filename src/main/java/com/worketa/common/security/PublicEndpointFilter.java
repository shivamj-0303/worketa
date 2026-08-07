package com.worketa.common.security;

import java.io.IOException;

import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Filter to allow public endpoints without authentication
 */
public class PublicEndpointFilter extends OncePerRequestFilter {

    private static final String[] PUBLIC_PATHS = {
        "/api/health",
        "/api/organisations/register",
        "/api/auth/login",
        "/api/auth/refresh",
        "/actuator"
    };

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String path = request.getRequestURI();
        
        // If it's a public endpoint, just continue
        for (String publicPath : PUBLIC_PATHS) {
            if (path.startsWith(publicPath)) {
                filterChain.doFilter(request, response);
                return;
            }
        }
        
        // Otherwise continue normal flow
        filterChain.doFilter(request, response);
    }
}
