package com.worketa.modules.employees.service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.worketa.common.exception.ApiException;
import com.worketa.common.security.OrganisationContext;
import com.worketa.modules.employees.entity.Employee;
import com.worketa.modules.employees.repository.EmployeeRepository;

@Service
public class EmployeeService {

    private final EmployeeRepository repo;

    public EmployeeService(EmployeeRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public Employee create(Employee emp) {
        var orgId = OrganisationContext.get();

        if (orgId == null) {
            throw new ApiException("Organisation context required");
        }

        if (emp.getJoiningDate().isAfter(LocalDate.now())) {
            throw new ApiException("Joining date cannot be in future");
        }

        emp.setOrganisationId(orgId);

        // IMPORTANT FIX
        if (emp.getEmployeeCode() == null || emp.getEmployeeCode().isBlank()) {
            emp.setEmployeeCode(generateEmployeeCode());
        }

        return repo.save(emp);
    }

    @Transactional
    public Employee update(UUID id, Employee emp) {
        Employee existing = getById(id);
        existing.setFullName(emp.getFullName());
        existing.setPhone(emp.getPhone());
        existing.setType(emp.getType());
        existing.setJoiningDate(emp.getJoiningDate());
        existing.setDailyWage(emp.getDailyWage());
        existing.setActive(emp.isActive());
        return repo.save(existing);
    }

    @Transactional
    public void delete(UUID id) {
        repo.deleteById(id);
    }

    @Transactional
    public Employee markInactive(UUID empId, LocalDate leavingDate) {
        var emp = repo.findByIdAndOrganisationId(empId, OrganisationContext.get())
                .orElseThrow(() -> new ApiException("Employee not found"));
        
        if (leavingDate.isBefore(emp.getJoiningDate())) {
            throw new ApiException("Leaving date cannot be before joining date");
        }
        
        emp.setActive(false);
        emp.setLeavingDate(leavingDate);
        return repo.save(emp);
    }

    public List<Employee> listByOrg() {
        return repo.findByOrganisationId(OrganisationContext.get());
    }

    public Employee getById(UUID id) {
        return repo.findByIdAndOrganisationId(id, OrganisationContext.get())
                .orElseThrow(() -> new ApiException("Employee not found"));
    }

    private String generateEmployeeCode() {
        long count = repo.count() + 1;

        return "EMP-" + String.format("%04d", count);
    }
}
