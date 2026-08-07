package com.worketa.modules.dashboard.service;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.worketa.common.exception.ApiException;
import com.worketa.common.security.OrganisationContext;
import com.worketa.modules.dashboard.dto.DashboardStatsResponse;
import com.worketa.modules.dashboard.repository.DashboardRepository;
import com.worketa.modules.employees.entity.Employee;

@Service
public class DashboardService {

    private final DashboardRepository repository;

    public DashboardService(
            DashboardRepository repository
    ) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public DashboardStatsResponse getStats() {

        UUID organisationId = OrganisationContext.get();

        if (organisationId == null) {
            throw new ApiException(
                "Organisation context required"
            );
        }

        DashboardStatsResponse stats =
                new DashboardStatsResponse();

        long totalEmployees =
                repository.countByOrganisationId(
                    organisationId
                );

        long activeEmployees =
                repository.countByOrganisationIdAndActiveTrue(
                    organisationId
                );

        long inactiveEmployees =
                repository.countByOrganisationIdAndActiveFalse(
                    organisationId
                );

        long totalDrivers =
                repository.countByOrganisationIdAndType(
                    organisationId,
                    Employee.EmployeeType.DRIVER
                );

        long totalAssistants =
                repository.countByOrganisationIdAndType(
                    organisationId,
                    Employee.EmployeeType.ASSISTANT
                );

        BigDecimal totalDailyWage =
                repository.getTotalDailyWage(
                    organisationId
                );

        BigDecimal activeDailyWage =
                repository.getActiveEmployeesDailyWage(
                    organisationId
                );

        stats.setTotalEmployees(totalEmployees);
        stats.setActiveEmployees(activeEmployees);
        stats.setInactiveEmployees(inactiveEmployees);

        stats.setTotalDrivers(totalDrivers);
        stats.setTotalAssistants(totalAssistants);

        stats.setTotalDailyWage(
            totalDailyWage == null
                ? BigDecimal.ZERO
                : totalDailyWage
        );

        stats.setActiveEmployeesDailyWage(
            activeDailyWage == null
                ? BigDecimal.ZERO
                : activeDailyWage
        );

        return stats;
    }
}