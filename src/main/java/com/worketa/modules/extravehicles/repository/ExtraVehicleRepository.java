package com.worketa.modules.extravehicles.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.worketa.modules.extravehicles.entity.ExtraVehicle;

public interface ExtraVehicleRepository extends JpaRepository<ExtraVehicle, UUID> {
    List<ExtraVehicle> findByOrganisationIdOrderByVehicleDateDesc(UUID organisationId);
}