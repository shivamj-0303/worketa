package com.worketa.modules.advances.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.worketa.modules.advances.entity.Advance;

public interface AdvanceRepository extends JpaRepository<Advance, UUID> {

    List<Advance> findByOrganisationId(UUID organisationId);
    List<Advance> findByOrganisationIdAndStatus(UUID organisationId, Advance.AdvanceStatus status);

    List<Advance> findByEmployeeIdAndOrganisationId(UUID employeeId, UUID organisationId);

    List<Advance> findByEmployeeIdAndOrganisationIdAndSettledFalse(UUID employeeId, UUID organisationId);

        List<Advance> findByEmployeeIdAndOrganisationIdAndSettledFalseAndAdvanceDateBetween(
            UUID employeeId,
            UUID organisationId,
            LocalDate start,
            LocalDate end
        );

        List<Advance> findByEmployeeIdAndOrganisationIdAndStatusAndSettledFalseAndAdvanceDateBetween(
            UUID employeeId,
            UUID organisationId,
            Advance.AdvanceStatus status,
            LocalDate start,
            LocalDate end
        );

    List<Advance> findByEmployeeIdAndSettledFalseAndAdvanceDateBetween(
            UUID employeeId,
            LocalDate start,
            LocalDate end
    );
}
