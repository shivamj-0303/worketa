package com.worketa.modules.employees.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.worketa.modules.employees.entity.Employee;

public class EmployeeCreateRequest {
    private String fullName;
    private String phone;
    private Employee.EmployeeType type;
    private LocalDate joiningDate;
    private BigDecimal dailyWage = BigDecimal.ZERO;
    private String email;
    private String password;
    private boolean active = true;

    public boolean hasPassword() {
        return password != null && !password.isBlank();
    }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public Employee.EmployeeType getType() { return type; }
    public void setType(Employee.EmployeeType type) { this.type = type; }
    public LocalDate getJoiningDate() { return joiningDate; }
    public void setJoiningDate(LocalDate joiningDate) { this.joiningDate = joiningDate; }
    public BigDecimal getDailyWage() { return dailyWage; }
    public void setDailyWage(BigDecimal dailyWage) { this.dailyWage = dailyWage; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
