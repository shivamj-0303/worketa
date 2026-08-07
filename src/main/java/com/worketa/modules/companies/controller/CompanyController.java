package com.worketa.modules.companies.controller;

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
import org.springframework.web.bind.annotation.RestController;

import com.worketa.common.response.ApiResponse;
import com.worketa.modules.companies.entity.Company;
import com.worketa.modules.companies.service.CompanyService;

@RestController
@RequestMapping("/api/companies")
public class CompanyController {

    private final CompanyService service;

    public CompanyController(CompanyService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Company>> create(@RequestBody Company company) {
        Company created = service.create(company);
        return ResponseEntity.ok(ApiResponse.ok("Company created", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Company>> update(@PathVariable UUID id, @RequestBody Company company) {
        Company updated = service.update(id, company);
        return ResponseEntity.ok(ApiResponse.ok("Company updated", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Company deleted", null));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Company>>> list() {
        List<Company> companies = service.listByOrg();
        return ResponseEntity.ok(ApiResponse.ok("Companies retrieved", companies));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Company>> getById(@PathVariable UUID id) {
        Company company = service.getById(id);
        return ResponseEntity.ok(ApiResponse.ok("Company retrieved", company));
    }
}
