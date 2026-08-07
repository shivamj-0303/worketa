package com.worketa.modules.auth.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.worketa.modules.auth.entity.RefreshToken;
import com.worketa.modules.users.entity.User;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    Optional<RefreshToken> findByToken(String token);
    void deleteAllByUser(User user);
}
