package com.worketa.modules.dashboard.dto;

import java.math.BigDecimal;

public class DashboardStatsResponse {

    private long totalEmployees;
    private long activeEmployees;
    private long inactiveEmployees;

    private long totalDrivers;
    private long totalAssistants;

    private BigDecimal totalMonthlySalary;
    private BigDecimal activeEmployeesMonthlySalary;

    public DashboardStatsResponse() {
    }

    public long getTotalEmployees() {
        return totalEmployees;
    }

    public void setTotalEmployees(long totalEmployees) {
        this.totalEmployees = totalEmployees;
    }

    public long getActiveEmployees() {
        return activeEmployees;
    }

    public void setActiveEmployees(long activeEmployees) {
        this.activeEmployees = activeEmployees;
    }

    public long getInactiveEmployees() {
        return inactiveEmployees;
    }

    public void setInactiveEmployees(long inactiveEmployees) {
        this.inactiveEmployees = inactiveEmployees;
    }

    public long getTotalDrivers() {
        return totalDrivers;
    }

    public void setTotalDrivers(long totalDrivers) {
        this.totalDrivers = totalDrivers;
    }

    public long getTotalAssistants() {
        return totalAssistants;
    }

    public void setTotalAssistants(long totalAssistants) {
        this.totalAssistants = totalAssistants;
    }

    public BigDecimal getTotalMonthlySalary() {
        return totalMonthlySalary;
    }

    public void setTotalMonthlySalary(BigDecimal totalMonthlySalary) {
        this.totalMonthlySalary = totalMonthlySalary;
    }

    public BigDecimal getActiveEmployeesMonthlySalary() {
        return activeEmployeesMonthlySalary;
    }

    public void setActiveEmployeesMonthlySalary(
            BigDecimal activeEmployeesMonthlySalary) {
        this.activeEmployeesMonthlySalary = activeEmployeesMonthlySalary;
    }
}