package com.worketa.modules.employees.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import com.worketa.common.audit.Auditable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "employees", uniqueConstraints = {
    @UniqueConstraint(name = "uk_employee_code_org", columnNames = {"employee_code", "organisation_id"})
})
public class Employee extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "organisation_id", columnDefinition = "uuid", nullable = false)
    private UUID organisationId;

    @Column(name = "employee_code", nullable = false)
    private String employeeCode;

    @Column(nullable = false)
    private String fullName;

    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmployeeType type;

    @Column(nullable = false)
    private LocalDate joiningDate;

    private LocalDate leavingDate;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "daily_wage", nullable = false, precision = 10, scale = 2)
    private BigDecimal dailyWage = BigDecimal.ZERO;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getOrganisationId() { return organisationId; }
    public void setOrganisationId(UUID organisationId) { this.organisationId = organisationId; }
    public String getEmployeeCode() { return employeeCode; }
    public void setEmployeeCode(String employeeCode) { this.employeeCode = employeeCode; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public EmployeeType getType() { return type; }
    public void setType(EmployeeType type) { this.type = type; }
    public LocalDate getJoiningDate() { return joiningDate; }
    public void setJoiningDate(LocalDate joiningDate) { this.joiningDate = joiningDate; }
    public LocalDate getLeavingDate() { return leavingDate; }
    public void setLeavingDate(LocalDate leavingDate) { this.leavingDate = leavingDate; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public BigDecimal getDailyWage() { return dailyWage; }
    public void setDailyWage(BigDecimal dailyWage) { this.dailyWage = dailyWage; }

    public enum EmployeeType {
        DRIVER, ASSISTANT
    }
}
