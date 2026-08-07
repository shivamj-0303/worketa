package com.worketa.modules.auth.service;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.worketa.common.exception.ApiException;
import com.worketa.common.security.JwtTokenProvider;
import com.worketa.modules.auth.dto.LoginRequest;
import com.worketa.modules.auth.entity.RefreshToken;
import com.worketa.modules.auth.repository.RefreshTokenRepository;
import com.worketa.modules.users.entity.User;
import com.worketa.modules.users.repository.UserRepository;

@Service
public class AuthService {

    private final UserRepository userRepo;
    private final JwtTokenProvider jwt;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepo;

    public AuthService(UserRepository userRepo, JwtTokenProvider jwt, PasswordEncoder passwordEncoder, RefreshTokenRepository refreshTokenRepo) {
        this.userRepo = userRepo;
        this.jwt = jwt;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenRepo = refreshTokenRepo;
    }

    @Transactional
    public Map<String, String> login(LoginRequest req) {

        User user = userRepo.findByEmail(req.getEmail())
                .orElseThrow(() -> new ApiException("Invalid credentials"));

        if (!user.isActive()) {
            throw new ApiException("User is inactive");
        }

        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new ApiException("Invalid credentials");
        }

        var roleNames = user.getRoles()
                .stream()
                .map(r -> r.getName())
                .toList();

        var claims = Map.<String, Object>of(
                "roles", String.join(",", roleNames),
                "organisationId", user.getOrganisationId().toString()
        );

        String accessToken = jwt.createAccessToken(
                user.getId().toString(),
                claims
        );

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiresAt(OffsetDateTime.now().plusDays(7));

        refreshTokenRepo.save(refreshToken);

        return Map.of(
                "accessToken", accessToken,
                "refreshToken", refreshToken.getToken()
        );
    }

    @Transactional
    public String refreshAccessToken(String refreshTokenStr) {
        RefreshToken refreshToken = refreshTokenRepo.findByToken(refreshTokenStr)
                .orElseThrow(() -> new ApiException("Invalid refresh token"));
        
        if (refreshToken.isRevoked()) throw new ApiException("Refresh token revoked");
        if (OffsetDateTime.now().isAfter(refreshToken.getExpiresAt())) throw new ApiException("Refresh token expired");
        
        User user = refreshToken.getUser();
        var roleNames = user.getRoles().stream().map(r -> r.getName()).toList();
        var claims = Map.<String, Object>of("roles", String.join(",", roleNames), "organisationId", user.getOrganisationId().toString());
        return jwt.createAccessToken(user.getId().toString(), claims);
    }
}
