package com.worketa.modules.advances.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.worketa.common.response.ApiResponse;
import com.worketa.modules.advances.entity.Advance;
import com.worketa.modules.advances.service.AdvanceService;

@RestController
@RequestMapping("/api/v1/advances")
public class AdvanceController {

    private final AdvanceService advanceService;

    public AdvanceController(AdvanceService advanceService) {
        this.advanceService = advanceService;
    }

    /**
     * Get all advances for the current organization
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<Advance>>> listAdvances() {
        List<Advance> advances = advanceService.listByOrg();
        return ResponseEntity.ok(ApiResponse.ok("Advances retrieved successfully", advances));
    }

    /**
     * Get advances for a specific employee
     */
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<ApiResponse<List<Advance>>> getEmployeeAdvances(
            @PathVariable UUID employeeId) {
        List<Advance> advances = advanceService.listByEmployee(employeeId);
        return ResponseEntity.ok(ApiResponse.ok("Employee advances retrieved successfully", advances));
    }

    /**
     * Get unsettled advances for a specific employee
     */
    @GetMapping("/employee/{employeeId}/unsettled")
    public ResponseEntity<ApiResponse<List<Advance>>> getUnsettledAdvances(
            @PathVariable UUID employeeId) {
        List<Advance> advances = advanceService.getUnsettledByEmployee(employeeId);
        return ResponseEntity.ok(ApiResponse.ok("Unsettled advances retrieved successfully", advances));
    }

    /**
     * Create a new advance
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Advance>> createAdvance(@RequestBody Advance advance) {
        Advance created = advanceService.create(advance);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Advance created successfully", created));
    }

    /**
     * Update an existing advance
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Advance>> updateAdvance(
            @PathVariable UUID id,
            @RequestBody Advance advance) {
        advance.setId(id);
        Advance updated = advanceService.create(advance);
        return ResponseEntity.ok(ApiResponse.ok("Advance updated successfully", updated));
    }

    /**
     * Delete an advance
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAdvance(@PathVariable UUID id) {
        advanceService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Advance deleted successfully", null));
    }
}
