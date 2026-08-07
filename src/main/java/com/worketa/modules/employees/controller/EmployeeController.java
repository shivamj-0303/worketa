package com.worketa.modules.employees.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.worketa.common.response.ApiResponse;
import com.worketa.modules.employees.entity.Employee;
import com.worketa.modules.employees.service.EmployeeService;

@RestController
@RequestMapping("/api/v1/employees")
public class EmployeeController {

    private final EmployeeService service;

    public EmployeeController(EmployeeService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Employee>> create(@RequestBody Employee emp) {
        Employee employee = service.create(emp);
        return ResponseEntity.ok(ApiResponse.ok("Employee created", employee));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Employee>> update(
            @PathVariable UUID id,
            @RequestBody Employee emp) {
        Employee employee = service.update(id, emp);
        return ResponseEntity.ok(ApiResponse.ok("Employee updated", employee));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Employee deleted", null));
    }

    @PostMapping("/{id}/mark-inactive")
    public ResponseEntity<ApiResponse<Employee>> markInactive(@PathVariable UUID id, @RequestParam LocalDate leavingDate) {
        Employee employee = service.markInactive(id, leavingDate);
        return ResponseEntity.ok(ApiResponse.ok("Employee marked inactive", employee));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Employee>>> list() {
        List<Employee> employees = service.listByOrg();
        return ResponseEntity.ok(ApiResponse.ok("Employees retrieved", employees));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Employee>> getById(@PathVariable UUID id) {
        Employee employee = service.getById(id);
        return ResponseEntity.ok(ApiResponse.ok("Employee retrieved", employee));
    }
}
