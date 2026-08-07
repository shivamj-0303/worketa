package com.worketa.modules.payroll.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.worketa.common.response.ApiResponse;
import com.worketa.modules.payroll.dto.PayrollBillUpdateRequest;
import com.worketa.modules.payroll.dto.PayrollSettleRequest;
import com.worketa.modules.payroll.dto.PayrollSummaryResponse;
import com.worketa.modules.payroll.entity.Payroll;
import com.worketa.modules.payroll.service.PayrollService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/payroll")
public class PayrollController {

    private final PayrollService service;

    public PayrollController(PayrollService service) {
        this.service = service;
    }

    @GetMapping("/summary/{employeeId}")
    public ResponseEntity<ApiResponse<PayrollSummaryResponse>> getSummary(
            @PathVariable UUID employeeId) {

        PayrollSummaryResponse response =
                service.getPayrollSummary(employeeId);

        return ResponseEntity.ok(
                ApiResponse.ok("Payroll summary generated", response)
        );
    }

    @PostMapping("/settle")
    public ResponseEntity<ApiResponse<Payroll>> settle(
            @Valid @RequestBody PayrollSettleRequest request) {

        Payroll payroll =
                service.settlePayroll(request.getEmployeeId());

        return ResponseEntity.ok(
                ApiResponse.ok("Payroll settled successfully", payroll)
        );
    }

    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<Payroll>>> getHistory() {
        List<Payroll> history = service.listHistory();

        return ResponseEntity.ok(
                ApiResponse.ok("Payroll history fetched", history)
        );
    }

    @GetMapping("/history/{id}")
    public ResponseEntity<ApiResponse<Payroll>> getHistoryById(
            @PathVariable UUID id) {

        Payroll history = service.getHistoryById(id);

        return ResponseEntity.ok(
                ApiResponse.ok("Payroll bill fetched", history)
        );
    }

    @PutMapping("/history/{id}")
    public ResponseEntity<ApiResponse<Payroll>> updateHistory(
            @PathVariable UUID id,
            @Valid @RequestBody PayrollBillUpdateRequest request) {

        Payroll history = service.updateHistory(id, request);

        return ResponseEntity.ok(
                ApiResponse.ok("Payroll bill updated successfully", history)
        );
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<ApiResponse<List<Payroll>>> getPayrollHistory(
            @PathVariable UUID employeeId) {

        List<Payroll> history =
                service.listByEmployee(employeeId);

        return ResponseEntity.ok(
                ApiResponse.ok("Payroll history fetched", history)
        );
    }
}