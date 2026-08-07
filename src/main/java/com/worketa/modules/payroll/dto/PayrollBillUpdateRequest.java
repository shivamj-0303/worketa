package com.worketa.modules.payroll.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class PayrollBillUpdateRequest {

    @NotBlank
    private String employeeName;

    @NotNull
    private LocalDate periodStart;

    @NotNull
    private LocalDate periodEnd;

    @NotNull
    private BigDecimal dailyWage;

    private int presentDays;

    private int doubledDays;

    private int absentDays;

    @NotNull
    private BigDecimal grossAmount;

    @NotNull
    private BigDecimal advanceDeduction;

    @NotNull
    private BigDecimal netAmount;

    @NotNull
    private LocalDateTime settledAt;

    private String advancesSnapshot;

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