package com.worketa.modules.organisation.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public class OrganisationResponse {
    private UUID id;
    private String name;
    private String email;
    private String phone;
    private OffsetDateTime createdAt;

    public OrganisationResponse(UUID id, String name, String email, String phone, OffsetDateTime createdAt) {
        this.id = id; this.name = name; this.email = email; this.phone = phone; this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
