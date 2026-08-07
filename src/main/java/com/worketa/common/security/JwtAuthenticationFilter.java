package com.worketa.common.security;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;

    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            String path = request.getRequestURI();
            String header = request.getHeader(HttpHeaders.AUTHORIZATION);
            
            // Only process if Bearer token is present
            if (header != null && header.startsWith("Bearer ")) {
                String token = header.substring(7);
                try {
                    var claims = tokenProvider.parseClaims(token);
                    String sub = claims.getSubject();
                    String roles = (String) claims.get("roles");
                    String orgId = (String) claims.get("organisationId");

                    List<SimpleGrantedAuthority> authorities = Arrays.stream(roles.split(","))
                            .map(String::trim)
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toList());

                    var auth = new UsernamePasswordAuthenticationToken(sub, null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(auth);

                    if (orgId != null) {
                        OrganisationContext.set(UUID.fromString(orgId));
                    }
                } catch (Exception ex) {
                    // Invalid token — clear context and continue
                    SecurityContextHolder.clearContext();
                }
            }
            // No Bearer token present — skip authentication (public endpoints will handle it)
            
            filterChain.doFilter(request, response);
        } finally {
            OrganisationContext.clear();
        }
    }
}
