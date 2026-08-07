package com.worketa.common.security;

import java.security.Key;
import java.util.Date;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtTokenProvider {

    private final JwtProperties props;
    private final Key key;

    public JwtTokenProvider(JwtProperties props) {
        this.props = props;
        this.key = Keys.hmacShaKeyFor(props.getSecret().getBytes());
    }

    public String createAccessToken(String subject, java.util.Map<String, Object> claims) {
        var now = System.currentTimeMillis();
        var b = Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + props.getAccessTokenExpirationMs()))
                .signWith(key, SignatureAlgorithm.HS256);
        if (claims != null) b.addClaims(claims);
        return b.compact();
    }

    public io.jsonwebtoken.Claims parseClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }
}
