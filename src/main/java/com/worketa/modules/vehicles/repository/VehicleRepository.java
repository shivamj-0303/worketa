package com.worketa.modules.vehicles.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.worketa.modules.vehicles.entity.Vehicle;

public interface VehicleRepository extends JpaRepository<Vehicle, UUID> {
    List<Vehicle> findByOrganisationId(UUID organisationId);
    Optional<Vehicle> findByIdAndOrganisationId(UUID id, UUID organisationId);
    Optional<Vehicle> findByVehicleNumber(String vehicleNumber);
    List<Vehicle> findByOrganisationIdAndActiveTrue(UUID organisationId);
}
