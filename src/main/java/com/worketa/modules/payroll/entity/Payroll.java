package com.worketa.modules.payroll.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import com.worketa.common.audit.Auditable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "payroll")
public class Payroll extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private UUID organisationId;

    private UUID employeeId;

    @Column(name = "employee_name")
    private String employeeName;

    @Column(name = "month")
    private String month;

    @Column(name = "base_salary")
    private BigDecimal baseSalary = BigDecimal.ZERO;

    private LocalDate periodStart;

    private LocalDate periodEnd;

    private BigDecimal dailyWage = BigDecimal.ZERO;

    private int presentDays;

    private int doubledDays;

    private int absentDays;

    private BigDecimal grossAmount = BigDecimal.ZERO;

    private BigDecimal advanceDeduction = BigDecimal.ZERO;

    private BigDecimal netAmount = BigDecimal.ZERO;

    @Column(name = "remarks")
    private String remarks;

    private LocalDateTime settledAt;

    @Column(name = "advances_snapshot", columnDefinition = "text")
    private String advancesSnapshot = "[]";

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getOrganisationId() {
        return organisationId;
    }

    public void setOrganisationId(UUID organisationId) {
        this.organisationId = organisationId;
    }

    public UUID getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(UUID employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public BigDecimal getBaseSalary() {
        return baseSalary;
    }

    public void setBaseSalary(BigDecimal baseSalary) {
        this.baseSalary = baseSalary;
    }

    public LocalDate getPeriodStart() {
        return periodStart;
    }

    public void setPeriodStart(LocalDate periodStart) {
        this.periodStart = periodStart;
    }

    public LocalDate getPeriodEnd() {
        return periodEnd;
    }

    public void setPeriodEnd(LocalDate periodEnd) {
        this.periodEnd = periodEnd;
    }

    public BigDecimal getDailyWage() {
        return dailyWage;
    }

    public void setDailyWage(BigDecimal dailyWage) {
        this.dailyWage = dailyWage;
    }

    public int getPresentDays() {
        return presentDays;
    }

    public void setPresentDays(int presentDays) {
        this.presentDays = presentDays;
    }

    public int getDoubledDays() {
        return doubledDays;
    }

    public void setDoubledDays(int doubledDays) {
        this.doubledDays = doubledDays;
    }

    public int getAbsentDays() {
        return absentDays;
    }

    public void setAbsentDays(int absentDays) {
        this.absentDays = absentDays;
    }

    public BigDecimal getGrossAmount() {
        return grossAmount;
    }

    public void setGrossAmount(BigDecimal grossAmount) {
        this.grossAmount = grossAmount;
    }

    public BigDecimal getAdvanceDeduction() {
        return advanceDeduction;
    }

    public void setAdvanceDeduction(BigDecimal advanceDeduction) {
        this.advanceDeduction = advanceDeduction;
    }

    public BigDecimal getNetAmount() {
        return netAmount;
    }

    public void setNetAmount(BigDecimal netAmount) {
        this.netAmount = netAmount;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public LocalDateTime getSettledAt() {
        return settledAt;
    }

    public void setSettledAt(LocalDateTime settledAt) {
        this.settledAt = settledAt;
    }

    public String getAdvancesSnapshot() {
        return advancesSnapshot;
    }

    public void setAdvancesSnapshot(String advancesSnapshot) {
        this.advancesSnapshot = advancesSnapshot;
    }
}
