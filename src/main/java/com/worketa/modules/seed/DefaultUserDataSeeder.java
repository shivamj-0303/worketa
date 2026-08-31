package com.worketa.modules.seed;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.worketa.modules.employees.entity.Employee;
import com.worketa.modules.employees.repository.EmployeeRepository;
import com.worketa.modules.organisation.entity.Organisation;
import com.worketa.modules.organisation.repository.OrganisationRepository;
import com.worketa.modules.users.entity.User;
import com.worketa.modules.users.repository.UserRepository;

@Component
public class DefaultUserDataSeeder implements CommandLineRunner {

    @Value("${worketa.seed.demo-data:false}")
    private boolean seedDemoData;

    private final OrganisationRepository organisationRepository;
    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    public DefaultUserDataSeeder(
            OrganisationRepository organisationRepository,
            UserRepository userRepository,
            EmployeeRepository employeeRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.organisationRepository = organisationRepository;
        this.userRepository = userRepository;
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (!seedDemoData) {
            return;
        }

        Organisation organisation = organisationRepository.findAll()
                .stream()
                .findFirst()
                .orElseGet(() -> {
                    Organisation newOrg = new Organisation();
                    newOrg.setName("Worketa Demo Org");
                    newOrg.setEmail("demo@worketa.com");
                    newOrg.setPhone("+91 90000 00000");
                    return organisationRepository.save(newOrg);
                });

        ensureUser(
                organisation.getId(),
                "admin@worketa.com",
                "Admin User",
                "ADMIN",
                "password123"
        );

        ensureEmployee(
                organisation.getId(),
                "Demo Employee",
                "EMP-1001",
                "employee@worketa.com",
                "password123"
        );
    }

    private void ensureUser(UUID organisationId, String email, String fullName, String role, String rawPassword) {
        if (userRepository.existsByEmailAndOrganisationId(email, organisationId)) {
            return;
        }

        User user = new User();
        user.setOrganisationId(organisationId);
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRoles(role);
        user.setActive(true);
        userRepository.save(user);
    }

    private void ensureEmployee(UUID organisationId, String fullName, String employeeCode, String email, String password) {
        if (userRepository.existsByEmailAndOrganisationId(email, organisationId)) {
            return;
        }

        ensureUser(organisationId, email, fullName, "EMPLOYEE", password);

        if (employeeRepository.findByOrganisationIdAndFullName(organisationId, fullName).isPresent()) {
            return;
        }

        Employee employee = new Employee();
        employee.setOrganisationId(organisationId);
        employee.setFullName(fullName);
        employee.setEmployeeCode(employeeCode);
        employee.setType(Employee.EmployeeType.DRIVER);
        employee.setJoiningDate(LocalDate.now().minusDays(30));
        employee.setDailyWage(new BigDecimal("500.00"));
        employee.setActive(true);
        employeeRepository.save(employee);
    }
}
