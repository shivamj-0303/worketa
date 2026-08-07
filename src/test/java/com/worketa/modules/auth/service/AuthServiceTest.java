package com.worketa.modules.auth.service;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.worketa.common.exception.ApiException;
import com.worketa.common.security.JwtTokenProvider;
import com.worketa.modules.auth.dto.LoginRequest;
import com.worketa.modules.auth.repository.RefreshTokenRepository;
import com.worketa.modules.users.entity.Role;
import com.worketa.modules.users.entity.User;
import com.worketa.modules.users.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepo;

    @Mock
    private JwtTokenProvider jwt;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RefreshTokenRepository refreshTokenRepo;

    @InjectMocks
    private AuthService service;

    private User testUser;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        UUID orgId = UUID.randomUUID();
        
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setEmail("user@test.com");
        testUser.setPassword("hashedpassword");
        testUser.setOrganisationId(orgId);
        testUser.setActive(true);
        
        Role role = new Role();
        role.setName("ADMIN");
        testUser.setRoles(Set.of(role));
        
        loginRequest = new LoginRequest();
        loginRequest.setEmail("user@test.com");
        loginRequest.setPassword("rawpassword");
        loginRequest.setOrganisationId(orgId.toString());
    }

    @Test
    void testLoginSuccess() {
        when(userRepo.findByEmail(eq("user@test.com")))
                .thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("rawpassword", "hashedpassword")).thenReturn(true);
        when(jwt.createAccessToken(anyString(), anyMap())).thenReturn("access_token");
        when(refreshTokenRepo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Map<String, String> result = service.login(loginRequest);

        assertNotNull(result);
        assertTrue(result.containsKey("accessToken"));
        assertTrue(result.containsKey("refreshToken"));
        verify(jwt, times(1)).createAccessToken(anyString(), anyMap());
    }

    @Test
    void testLoginInvalidCredentials() {
        when(userRepo.findByEmail(eq("user@test.com")))
                .thenReturn(Optional.empty());

        assertThrows(ApiException.class, () -> service.login(loginRequest));
    }

    @Test
    void testLoginInactiveUser() {
        testUser.setActive(false);
        when(userRepo.findByEmail(eq("user@test.com")))
                .thenReturn(Optional.of(testUser));

        assertThrows(ApiException.class, () -> service.login(loginRequest));
    }
}
