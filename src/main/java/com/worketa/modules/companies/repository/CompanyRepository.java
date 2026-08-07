package com.worketa.modules.companies.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.worketa.modules.companies.entity.Company;

public interface CompanyRepository extends JpaRepository<Company, UUID> {
    List<Company> findByOrganisationId(UUID organisationId);
    Optional<Company> findByIdAndOrganisationId(UUID id, UUID organisationId);
    List<Company> findByOrganisationIdAndActiveTrue(UUID organisationId);
}
