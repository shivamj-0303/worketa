package com.worketa.modules.payroll.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public class PayrollSettleRequest {

    @NotNull
    private UUID employeeId;

    public UUID getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(UUID employeeId) {
        this.employeeId = employeeId;
    }
}
