package com.worketa.common.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "security.jwt")
public class JwtProperties {
    private String secret;
    private long accessTokenExpirationMs = 15 * 60 * 1000; // 15 min
    private long refreshTokenExpirationMs = 7 * 24 * 60 * 60 * 1000; // 7 days

    public String getSecret() { return secret; }
    public void setSecret(String secret) { this.secret = secret; }
    public long getAccessTokenExpirationMs() { return accessTokenExpirationMs; }
    public void setAccessTokenExpirationMs(long v) { this.accessTokenExpirationMs = v; }
    public long getRefreshTokenExpirationMs() { return refreshTokenExpirationMs; }
    public void setRefreshTokenExpirationMs(long v) { this.refreshTokenExpirationMs = v; }
}
