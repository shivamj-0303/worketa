package com.worketa.modules.employees.dto;

import com.worketa.modules.employees.entity.Employee;

public class EmployeeCreateResponse {

    private Employee employee;
    private String generatedPassword;

    public EmployeeCreateResponse() {
    }

    public EmployeeCreateResponse(Employee employee, String generatedPassword) {
        this.employee = employee;
        this.generatedPassword = generatedPassword;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public String getGeneratedPassword() {
        return generatedPassword;
    }

    public void setGeneratedPassword(String generatedPassword) {
        this.generatedPassword = generatedPassword;
    }
}