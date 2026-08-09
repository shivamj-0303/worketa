package com.worketa.common.security;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.Map;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtTokenProvider {

    private final JwtProperties props;
    private final Key key;

    public JwtTokenProvider(JwtProperties props) {

        this.props = props;

        String secret =
                props.getSecret();

        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException(
                    "JWT_SECRET is not configured"
            );
        }

        byte[] secretBytes =
                secret.getBytes(StandardCharsets.UTF_8);

        /*
         * HS256 requires at least 256 bits = 32 bytes.
         */
        if (secretBytes.length < 32) {
            throw new IllegalStateException(
                    "JWT_SECRET must be at least 32 bytes for HS256"
            );
        }

        this.key =
                Keys.hmacShaKeyFor(secretBytes);
    }

    /**
     * Creates a JWT access token.
     *
     * This method is called AFTER successful login.
     */
    public String createAccessToken(
            String subject,
            Map<String, Object> claims
    ) {

        if (subject == null || subject.isBlank()) {
            throw new IllegalArgumentException(
                    "JWT subject cannot be empty"
            );
        }

        long now =
                System.currentTimeMillis();

        var builder =
                Jwts.builder()
                        .setSubject(subject)
                        .setIssuedAt(
                                new Date(now)
                        )
                        .setExpiration(
                                new Date(
                                        now
                                                + props
                                                .getAccessTokenExpirationMs()
                                )
                        )
                        .signWith(
                                key,
                                SignatureAlgorithm.HS256
                        );

        /*
         * Add custom claims such as:
         *
         * organisationId
         * roles
         */
        if (claims != null
                && !claims.isEmpty()) {

            builder.addClaims(claims);
        }

        return builder.compact();
    }

    /**
     * Parses and verifies a JWT.
     *
     * IMPORTANT:
     *
     * This method should ONLY be called when a request
     * actually contains a Bearer token.
     */
    public Claims parseClaims(String token) {

        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException(
                    "JWT token is empty"
            );
        }

        return Jwts
                .parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Safely validates a JWT.
     *
     * Returns false instead of throwing an exception.
     */
    public boolean validateToken(String token) {

        if (token == null || token.isBlank()) {
            return false;
        }

        try {

            parseClaims(token);

            return true;

        } catch (Exception ex) {

            return false;
        }
    }
}
