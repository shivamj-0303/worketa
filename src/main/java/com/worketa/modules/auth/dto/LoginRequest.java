package com.worketa.modules.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class LoginRequest {
    @NotBlank
    @Email
    private String email;
    @NotBlank
    private String password;
    private String organisationId;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getOrganisationId() { return organisationId; }
    public void setOrganisationId(String organisationId) { this.organisationId = organisationId; }
}
