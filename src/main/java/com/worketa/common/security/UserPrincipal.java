package com.worketa.common.security;

import java.util.Set;
import java.util.UUID;

public class UserPrincipal {
    private UUID id;
    private String email;
    private UUID organisationId;
    private Set<String> roles;

    public UserPrincipal(UUID id, String email, UUID organisationId, Set<String> roles) {
        this.id = id;
        this.email = email;
        this.organisationId = organisationId;
        this.roles = roles;
    }

    public UUID getId() { return id; }
    public String getEmail() { return email; }
    public UUID getOrganisationId() { return organisationId; }
    public Set<String> getRoles() { return roles; }
}
