package com.worketa.common.security;

import java.io.IOException;
import java.util.ArrayList;
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

import io.jsonwebtoken.Claims;
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

    /**
     * Endpoints that must NEVER require JWT authentication.
     *
     * Login is especially important because the user does not have
     * an access token before successfully logging in.
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {

        String path = request.getServletPath();

        return path.equals("/api/auth/login")
                || path.equals("/api/auth/register")
                || path.equals("/api/auth/refresh")
                || path.equals("/error");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        try {

            String header =
                    request.getHeader(HttpHeaders.AUTHORIZATION);

            /*
             * No Authorization header.
             *
             * This is completely valid for public endpoints.
             * Spring Security will decide whether the endpoint
             * itself requires authentication.
             */
            if (header == null || header.isBlank()) {
                filterChain.doFilter(request, response);
                return;
            }

            /*
             * Authorization header exists but is not a Bearer token.
             *
             * Do not attempt to parse it as JWT.
             */
            if (!header.startsWith("Bearer ")) {
                filterChain.doFilter(request, response);
                return;
            }

            String token = header.substring(7).trim();

            /*
             * Empty Bearer token.
             */
            if (token.isBlank()) {
                filterChain.doFilter(request, response);
                return;
            }

            try {

                /*
                 * Parse and verify JWT signature.
                 */
                Claims claims =
                        tokenProvider.parseClaims(token);

                String subject =
                        claims.getSubject();

                if (subject == null || subject.isBlank()) {
                    throw new IllegalArgumentException(
                            "JWT does not contain a subject"
                    );
                }

                /*
                 * -------------------------------------------------
                 * ORGANISATION
                 * -------------------------------------------------
                 */

                String organisationId =
                        claims.get(
                                "organisationId",
                                String.class
                        );

                if (organisationId != null
                        && !organisationId.isBlank()) {

                    try {

                        OrganisationContext.set(
                                UUID.fromString(organisationId)
                        );

                    } catch (IllegalArgumentException ex) {

                        System.err.println(
                                "Invalid organisationId in JWT: "
                                        + organisationId
                        );

                        SecurityContextHolder.clearContext();

                        filterChain.doFilter(
                                request,
                                response
                        );

                        return;
                    }
                }

                /*
                 * -------------------------------------------------
                 * ROLES
                 * -------------------------------------------------
                 */

                List<SimpleGrantedAuthority> authorities =
                        extractAuthorities(claims);

                /*
                 * Create authenticated principal.
                 *
                 * Roles are NOT required for authentication.
                 */
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                subject,
                                null,
                                authorities
                        );

                SecurityContextHolder
                        .getContext()
                        .setAuthentication(authentication);

                System.out.println(
                        "JWT authenticated user: "
                                + subject
                                + " | roles: "
                                + authorities
                );

            } catch (Exception ex) {

                /*
                 * Invalid/expired/wrong-signature JWT.
                 *
                 * Clear authentication and allow Spring Security
                 * to decide whether this endpoint is public or
                 * protected.
                 */
                SecurityContextHolder.clearContext();

                System.err.println(
                        "JWT authentication failed: "
                                + ex.getClass().getSimpleName()
                                + " - "
                                + ex.getMessage()
                );

                /*
                 * IMPORTANT:
                 *
                 * We do NOT throw the JWT exception here.
                 * The request continues through the filter chain.
                 */
            }

            filterChain.doFilter(request, response);

        } finally {

            /*
             * Never leak organisation information between requests.
             */
            OrganisationContext.clear();
        }
    }

    /**
     * Extract roles from JWT.
     *
     * Supports:
     *
     * "ADMIN"
     *
     * "ADMIN,MANAGER"
     *
     * ["ADMIN", "MANAGER"]
     */
    private List<SimpleGrantedAuthority> extractAuthorities(
            Claims claims
    ) {

        Object rolesClaim =
                claims.get("roles");

        if (rolesClaim == null) {
            return new ArrayList<>();
        }

        /*
         * JWT:
         *
         * "roles": "ADMIN,MANAGER"
         */
        if (rolesClaim instanceof String rolesString) {

            if (rolesString.isBlank()) {
                return new ArrayList<>();
            }

            return Arrays.stream(
                            rolesString.split(",")
                    )
                    .map(String::trim)
                    .filter(role -> !role.isBlank())
                    .map(this::toAuthority)
                    .collect(Collectors.toList());
        }

        /*
         * JWT:
         *
         * "roles": ["ADMIN", "MANAGER"]
         */
        if (rolesClaim instanceof List<?> rolesList) {

            return rolesList.stream()
                    .filter(role -> role != null)
                    .map(Object::toString)
                    .map(String::trim)
                    .filter(role -> !role.isBlank())
                    .map(this::toAuthority)
                    .collect(Collectors.toList());
        }

        return new ArrayList<>();
    }

    /**
     * Converts:
     *
     * ADMIN
     *
     * into:
     *
     * ROLE_ADMIN
     *
     * This supports:
     *
     * @PreAuthorize("hasRole('ADMIN')")
     */
    private SimpleGrantedAuthority toAuthority(
            String role
    ) {

        if (role.startsWith("ROLE_")) {
            return new SimpleGrantedAuthority(role);
        }

        return new SimpleGrantedAuthority(
                "ROLE_" + role
        );
    }
}
