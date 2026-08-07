package com.worketa.modules.advances.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.worketa.modules.advances.entity.Advance;

public interface AdvanceRepository extends JpaRepository<Advance, UUID> {

    List<Advance> findByOrganisationId(UUID organisationId);

    List<Advance> findByEmployeeId(UUID employeeId);

    List<Advance> findByEmployeeIdAndSettledFalse(UUID employeeId);

    List<Advance> findByEmployeeIdAndSettledFalseAndAdvanceDateBetween(
            UUID employeeId,
            LocalDate start,
            LocalDate end
    );
}
