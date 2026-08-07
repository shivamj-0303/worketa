package com.worketa.common.security;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Simple rate limiting filter using sliding window counter.
 * 
 * CRITICAL SECURITY FEATURE:
 * - Prevents brute-force attacks on login endpoint
 * - Limits requests per IP address
 * - Config: 5 login attempts per 60 seconds per IP
 * 
 * This protects against:
 * - Brute-force password attacks
 * - Credential stuffing
 * - DDoS-style amplification attacks
 * 
 * Note: For production with distributed systems, use Redis-backed rate limiting.
 */
@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    // Map of IP -> list of request timestamps for sliding window
    private final Map<String, LinkedList<Long>> requestTimestamps = new ConcurrentHashMap<>();
    
    // Rate limit config
    private static final int LOGIN_REQUESTS_PER_MINUTE = 5;
    private static final long TIME_WINDOW_MS = 60_000;  // 1 minute

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        
        String clientIp = getClientIp(request);
        String path = request.getRequestURI();
        
        // Apply strict rate limiting to login endpoint
        if (path.contains("/auth/login") || path.contains("/organisations/register")) {
            if (!checkRateLimit(clientIp, LOGIN_REQUESTS_PER_MINUTE, TIME_WINDOW_MS)) {
                response.setStatus(429);  // HTTP 429 Too Many Requests
                response.setContentType("application/json");
                response.getWriter().write("{\"success\":false,\"message\":\"Too many requests. Please try again later.\"}");
                return;
            }
        }
        
        filterChain.doFilter(request, response);
    }

    /**
     * Check if request is within rate limit using sliding window counter.
     * 
     * @param key IP address
     * @param capacity Maximum requests
     * @param windowMs Time window in milliseconds
     * @return true if within limit, false if exceeded
     */
    private synchronized boolean checkRateLimit(String key, int capacity, long windowMs) {
        long now = System.currentTimeMillis();
        
        LinkedList<Long> timestamps = requestTimestamps.computeIfAbsent(key, k -> new LinkedList<>());
        
        // Remove old timestamps outside the window
        while (!timestamps.isEmpty() && timestamps.getFirst() < now - windowMs) {
            timestamps.removeFirst();
        }
        
        // Check if we're within limit
        if (timestamps.size() < capacity) {
            timestamps.addLast(now);
            return true;
        }
        
        return false;
    }

    /**
     * Get client IP address, handling proxies and load balancers.
     * 
     * IMPORTANT: In production, ensure your load balancer/reverse proxy
     * is sending the correct X-Forwarded-For header.
     */
    private String getClientIp(HttpServletRequest request) {
        String forwaredFor = request.getHeader("X-Forwarded-For");
        if (forwaredFor != null && !forwaredFor.isBlank()) {
            // X-Forwarded-For can have multiple IPs, take the first one
            return forwaredFor.split(",")[0].trim();
        }
        
        String clientIp = request.getHeader("X-Real-IP");
        if (clientIp != null && !clientIp.isBlank()) {
            return clientIp;
        }
        
        return request.getRemoteAddr();
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Don't apply rate limiting to health checks and static resources
        String path = request.getRequestURI();
        return path.startsWith("/actuator") || 
               path.startsWith("/swagger-ui") || 
               path.startsWith("/v3/api-docs") ||
               path.startsWith("/static");
    }
}
