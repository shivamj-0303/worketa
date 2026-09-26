package com.worketa.modules.dashboard.repository;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.worketa.modules.employees.entity.Employee;

public interface DashboardRepository
        extends JpaRepository<Employee, UUID> {

    long countByOrganisationId(UUID organisationId);

    long countByOrganisationIdAndActiveTrue(UUID organisationId);

    long countByOrganisationIdAndActiveFalse(UUID organisationId);

    long countByOrganisationIdAndType(
            UUID organisationId,
            Employee.EmployeeType type
    );

    long countByOrganisationIdAndActiveTrueAndType(
            UUID organisationId,
            Employee.EmployeeType type
    );

    @Query("""
        SELECT COALESCE(SUM(e.monthlySalary), 0)
        FROM Employee e
        WHERE e.organisationId = :organisationId
        """)
        BigDecimal getTotalMonthlySalary(UUID organisationId);

    @Query("""
        SELECT COALESCE(SUM(e.monthlySalary), 0)
        FROM Employee e
        WHERE e.organisationId = :organisationId
        AND e.active = true
        """)
        BigDecimal getActiveEmployeesMonthlySalary(
            UUID organisationId
    );
}