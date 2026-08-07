package com.worketa.modules.advances.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;

import com.worketa.common.audit.Auditable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "advances")
public class Advance extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "organisation_id", columnDefinition = "uuid", nullable = false)
    private UUID organisationId;

    @Column(name = "employee_id", columnDefinition = "uuid", nullable = false)
    private UUID employeeId;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private LocalDate advanceDate;

    private String note;

    private boolean settled = false;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getOrganisationId() { return organisationId; }
    public void setOrganisationId(UUID organisationId) { this.organisationId = organisationId; }
    public UUID getEmployeeId() { return employeeId; }
    public void setEmployeeId(UUID employeeId) { this.employeeId = employeeId; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public LocalDate getAdvanceDate() { return advanceDate; }
    public void setAdvanceDate(LocalDate advanceDate) { this.advanceDate = advanceDate; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public boolean isSettled() { return settled; }
    public void setSettled(boolean settled) { this.settled = settled; }
}
