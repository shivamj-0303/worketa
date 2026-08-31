package com.worketa.modules.users.service;

import java.util.List;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.worketa.common.exception.ApiException;
import com.worketa.common.security.OrganisationContext;
import com.worketa.modules.users.entity.User;
import com.worketa.modules.users.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository repo;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository repo,
            PasswordEncoder passwordEncoder
    ) {
        this.repo = repo;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User createUser(
            String fullName,
            String email,
            String password,
            String roleName
    ) {
        UUID orgId = OrganisationContext.get();

        if (repo.existsByEmailAndOrganisationId(email, orgId)) {
            throw new ApiException(
                    "User with email already exists in organisation"
            );
        }

        if (roleName == null || roleName.isBlank()) {
            throw new ApiException("Role is required");
        }

        User user = new User();

        user.setOrganisationId(orgId);
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRoles(roleName.trim().toUpperCase());
        user.setActive(true);

        return repo.save(user);
    }

    public List<User> listByOrg() {
        return repo.findAll();
    }

    public User getById(UUID id) {
        return repo.findById(id)
                .orElseThrow(() -> new ApiException("User not found"));
    }

    @Transactional
    public void changePassword(UUID userId, String currentPassword, String newPassword) {
        if (userId == null) {
            throw new ApiException("User id is required");
        }
        if (currentPassword == null || currentPassword.isBlank()) {
            throw new ApiException("Current password is required");
        }
        if (newPassword == null || newPassword.isBlank()) {
            throw new ApiException("New password is required");
        }
        if (newPassword.length() < 6) {
            throw new ApiException("New password must be at least 6 characters long");
        }

        User user = repo.findById(userId)
                .orElseThrow(() -> new ApiException("User not found"));

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new ApiException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        repo.save(user);
    }
}
