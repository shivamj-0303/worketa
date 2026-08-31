package com.worketa.modules.employees.service;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.worketa.common.exception.ApiException;
import com.worketa.common.security.OrganisationContext;
import com.worketa.modules.employees.dto.EmployeeCreateRequest;
import com.worketa.modules.employees.dto.EmployeeCreateResponse;
import com.worketa.modules.employees.entity.Employee;
import com.worketa.modules.employees.repository.EmployeeRepository;
import com.worketa.modules.users.entity.User;
import com.worketa.modules.users.repository.UserRepository;

@Service
public class EmployeeService {

    private final EmployeeRepository repo;
    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;

    public EmployeeService(EmployeeRepository repo, UserRepository userRepo, PasswordEncoder passwordEncoder) {
        this.repo = repo;
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public EmployeeCreateResponse create(EmployeeCreateRequest request) {
        var orgId = OrganisationContext.get();

        if (orgId == null) {
            throw new ApiException("Organisation context required");
        }

        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new ApiException("Employee email is required");
        }
        if (request.getJoiningDate() != null && request.getJoiningDate().isAfter(LocalDate.now())) {
            throw new ApiException("Joining date cannot be in future");
        }
        if (userRepo.existsByEmailAndOrganisationId(request.getEmail(), orgId)) {
            throw new ApiException("User with this email already exists in the organisation");
        }

        String generatedPassword = request.hasPassword() ? request.getPassword() : generateTemporaryPassword();

        Employee emp = new Employee();
        emp.setOrganisationId(orgId);
        emp.setFullName(request.getFullName());
        emp.setPhone(request.getPhone());
        emp.setType(request.getType());
        emp.setJoiningDate(request.getJoiningDate() != null ? request.getJoiningDate() : LocalDate.now());
        emp.setDailyWage(request.getDailyWage() != null ? request.getDailyWage() : BigDecimal.ZERO);
        emp.setActive(request.isActive());

        if (emp.getEmployeeCode() == null || emp.getEmployeeCode().isBlank()) {
            emp.setEmployeeCode(generateEmployeeCode());
        }

        Employee savedEmployee = repo.save(emp);

        User user = new User();
        user.setOrganisationId(orgId);
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(generatedPassword));
        user.setRoles("EMPLOYEE");
        user.setActive(true);
        userRepo.save(user);

        return new EmployeeCreateResponse(savedEmployee, generatedPassword);
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

    private String generateTemporaryPassword() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789@#*";
        SecureRandom random = new SecureRandom();
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < 10; i++) {
            int index = random.nextInt(chars.length());
            builder.append(chars.charAt(index));
        }

        return builder.toString();
    }
}
