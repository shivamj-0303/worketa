package com.worketa.modules.payroll.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.worketa.modules.payroll.entity.Payroll;

public interface PayrollRepository extends JpaRepository<Payroll, UUID> {

    List<Payroll> findByOrganisationId(UUID organisationId);

    List<Payroll> findByOrganisationIdOrderBySettledAtDesc(UUID organisationId);

    List<Payroll> findByEmployeeId(UUID employeeId);

    Optional<Payroll> findByIdAndOrganisationId(UUID id, UUID organisationId);

    Optional<Payroll> findTopByEmployeeIdAndOrganisationIdOrderByPeriodEndDesc(
        UUID employeeId,
        UUID organisationId
    );
}
