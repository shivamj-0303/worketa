package com.worketa.modules.employees.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.worketa.modules.employees.entity.WageHistory;

public interface WageHistoryRepository extends JpaRepository<WageHistory, UUID> {
    List<WageHistory> findByEmployeeId(UUID employeeId);
    Optional<WageHistory> findByEmployeeIdAndEffectiveFromLessThanEqualAndEffectiveToIsNullOrEffectiveToGreaterThanEqual(
            UUID employeeId, LocalDate date1, LocalDate date2);
}
