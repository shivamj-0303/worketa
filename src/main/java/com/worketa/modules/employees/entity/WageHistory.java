package com.worketa.modules.employees.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import com.worketa.common.audit.Auditable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(name = "wage_history", indexes = {
    @Index(name = "idx_wage_history_employee_org", columnList = "employee_id,organisation_id"),
    @Index(name = "idx_wage_history_effective_from", columnList = "effective_from")
})
public class WageHistory extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "organisation_id", columnDefinition = "uuid", nullable = false)
    private UUID organisationId;

    @Column(name = "employee_id", columnDefinition = "uuid", nullable = false)
    private UUID employeeId;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal dailyWage = BigDecimal.ZERO;

    @Column(nullable = false)
    private LocalDate effectiveFrom;

    private LocalDate effectiveTo;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getOrganisationId() { return organisationId; }
    public void setOrganisationId(UUID organisationId) { this.organisationId = organisationId; }
    public UUID getEmployeeId() { return employeeId; }
    public void setEmployeeId(UUID employeeId) { this.employeeId = employeeId; }
    public BigDecimal getDailyWage() { return dailyWage; }
    public void setDailyWage(BigDecimal dailyWage) { this.dailyWage = dailyWage; }
    public LocalDate getEffectiveFrom() { return effectiveFrom; }
    public void setEffectiveFrom(LocalDate effectiveFrom) { this.effectiveFrom = effectiveFrom; }
    public LocalDate getEffectiveTo() { return effectiveTo; }
    public void setEffectiveTo(LocalDate effectiveTo) { this.effectiveTo = effectiveTo; }
}
