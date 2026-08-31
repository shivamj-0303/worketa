package com.worketa.modules.employees.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.worketa.modules.employees.entity.Employee;

public interface EmployeeRepository extends JpaRepository<Employee, UUID> {
    List<Employee> findByOrganisationId(UUID organisationId);
    Optional<Employee> findByIdAndOrganisationId(UUID id, UUID organisationId);
    Optional<Employee> findByEmployeeCodeAndOrganisationId(String code, UUID organisationId);
    Optional<Employee> findByOrganisationIdAndFullName(UUID organisationId, String fullName);
    List<Employee> findByOrganisationIdAndActiveTrue(UUID organisationId);
}
