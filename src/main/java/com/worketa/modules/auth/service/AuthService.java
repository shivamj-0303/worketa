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
import com.worketa.modules.employees.entity.Employee;
import com.worketa.modules.employees.repository.EmployeeRepository;
import com.worketa.modules.users.entity.User;
import com.worketa.modules.users.repository.UserRepository;

@Service
public class AuthService {

    private final UserRepository userRepo;
    private final EmployeeRepository employeeRepo;
    private final JwtTokenProvider jwt;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepo;

    public AuthService(
            UserRepository userRepo,
            EmployeeRepository employeeRepo,
            JwtTokenProvider jwt,
            PasswordEncoder passwordEncoder,
            RefreshTokenRepository refreshTokenRepo
    ) {
        this.userRepo = userRepo;
        this.employeeRepo = employeeRepo;
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

        /*
         * roles is now a simple String column in users table.
         * Example: ADMIN, MANAGER, EMPLOYEE
         */
        String role = user.getRoles();

        if (role == null || role.isBlank()) {
            throw new ApiException("User role is not configured");
        }

        Employee employee = employeeRepo.findByOrganisationIdAndFullName(user.getOrganisationId(), user.getFullName())
                .orElse(null);

        var claims = new java.util.HashMap<String, Object>();
        claims.put("roles", role);
        claims.put("organisationId", user.getOrganisationId().toString());
        if (employee != null) {
            claims.put("employeeId", employee.getId().toString());
        }

        String accessToken = jwt.createAccessToken(
                user.getId().toString(),
                claims
        );

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiresAt(
                OffsetDateTime.now().plusDays(7)
        );

        refreshTokenRepo.save(refreshToken);

        var response = new java.util.HashMap<String, String>();
        response.put("accessToken", accessToken);
        response.put("refreshToken", refreshToken.getToken());
        response.put("userId", user.getId().toString());
        response.put("email", user.getEmail());
        response.put("fullName", user.getFullName());
        response.put("role", role);
        if (employee != null) {
            response.put("employeeId", employee.getId().toString());
        }
        return response;
    }

    @Transactional
    public String refreshAccessToken(String refreshTokenStr) {

        RefreshToken refreshToken = refreshTokenRepo.findByToken(refreshTokenStr)
                .orElseThrow(() -> new ApiException("Invalid refresh token"));

        if (refreshToken.isRevoked()) {
            throw new ApiException("Refresh token revoked");
        }

        if (OffsetDateTime.now().isAfter(refreshToken.getExpiresAt())) {
            throw new ApiException("Refresh token expired");
        }

        User user = refreshToken.getUser();

        String role = user.getRoles();

        if (role == null || role.isBlank()) {
            throw new ApiException("User role is not configured");
        }

        Employee employee = employeeRepo.findByOrganisationIdAndFullName(user.getOrganisationId(), user.getFullName())
                .orElse(null);

        var claims = new java.util.HashMap<String, Object>();
        claims.put("roles", role);
        claims.put("organisationId", user.getOrganisationId().toString());
        if (employee != null) {
            claims.put("employeeId", employee.getId().toString());
        }

        return jwt.createAccessToken(
                user.getId().toString(),
                claims
        );
    }
}
