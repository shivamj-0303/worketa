package com.worketa.modules.users.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.worketa.modules.users.entity.User;

public interface UserRepository extends JpaRepository<User, UUID> {
    boolean existsByEmailAndOrganisationId(String email, UUID organisationId);
    java.util.Optional<User> findByEmail(String email);
}
