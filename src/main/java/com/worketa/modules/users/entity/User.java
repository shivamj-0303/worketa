package com.worketa.modules.users.entity;

import java.util.UUID;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;

import com.worketa.common.audit.Auditable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "organisation_id", columnDefinition = "uuid", nullable = false)
    private UUID organisationId;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    // Roles relation
    @jakarta.persistence.ManyToMany
    @jakarta.persistence.JoinTable(name = "user_roles",
        joinColumns = @jakarta.persistence.JoinColumn(name = "user_id"),
        inverseJoinColumns = @jakarta.persistence.JoinColumn(name = "role_id"))
    private java.util.Set<com.worketa.modules.users.entity.Role> roles;
    private String phone;

    private boolean active = true;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getOrganisationId() { return organisationId; }
    public void setOrganisationId(UUID organisationId) { this.organisationId = organisationId; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public java.util.Set<com.worketa.modules.users.entity.Role> getRoles() { return roles; }
    public void setRoles(java.util.Set<com.worketa.modules.users.entity.Role> roles) { this.roles = roles; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
