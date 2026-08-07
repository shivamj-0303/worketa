package com.worketa.modules.organisation.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.worketa.modules.organisation.entity.Organisation;

public interface OrganisationRepository extends JpaRepository<Organisation, UUID> {
    boolean existsByEmail(String email);
}
