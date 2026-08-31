package com.worketa.modules.payroll.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class PayrollSummaryResponse {

    private UUID employeeId;

    private String employeeName;

    private LocalDate periodStart;

    private LocalDate periodEnd;

    private BigDecimal dailyWage;

    private int presentDays;

    private int doubledDays;

    private int absentDays;

    private int requestedPresentDays;
    private int requestedDoubledDays;
    private int requestedAbsentDays;

    private BigDecimal grossAmount;

    private BigDecimal bonusAmount;

    private BigDecimal advanceDeduction;

    private BigDecimal netAmount;

    private BigDecimal requestedAdvanceAmount = BigDecimal.ZERO;
    private BigDecimal requestedBonusAmount = BigDecimal.ZERO;

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

    public int getRequestedPresentDays() { return requestedPresentDays; }
    public void setRequestedPresentDays(int requestedPresentDays) { this.requestedPresentDays = requestedPresentDays; }
    public int getRequestedDoubledDays() { return requestedDoubledDays; }
    public void setRequestedDoubledDays(int requestedDoubledDays) { this.requestedDoubledDays = requestedDoubledDays; }
    public int getRequestedAbsentDays() { return requestedAbsentDays; }
    public void setRequestedAbsentDays(int requestedAbsentDays) { this.requestedAbsentDays = requestedAbsentDays; }

    public BigDecimal getGrossAmount() {
        return grossAmount;
    }

    public void setGrossAmount(BigDecimal grossAmount) {
        this.grossAmount = grossAmount;
    }

    public BigDecimal getBonusAmount() {
        return bonusAmount;
    }

    public void setBonusAmount(BigDecimal bonusAmount) {
        this.bonusAmount = bonusAmount;
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

    public BigDecimal getRequestedAdvanceAmount() { return requestedAdvanceAmount; }
    public void setRequestedAdvanceAmount(BigDecimal requestedAdvanceAmount) { this.requestedAdvanceAmount = requestedAdvanceAmount; }
    public BigDecimal getRequestedBonusAmount() { return requestedBonusAmount; }
    public void setRequestedBonusAmount(BigDecimal requestedBonusAmount) { this.requestedBonusAmount = requestedBonusAmount; }
}
