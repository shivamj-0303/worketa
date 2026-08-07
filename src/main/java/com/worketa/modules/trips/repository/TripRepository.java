package com.worketa.modules.trips.repository;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.worketa.modules.trips.entity.Trip;

public interface TripRepository extends JpaRepository<Trip, UUID> {
    List<Trip> findByOrganisationId(UUID organisationId);
    Optional<Trip> findByIdAndOrganisationId(UUID id, UUID organisationId);
    List<Trip> findByVehicleIdAndTripDate(UUID vehicleId, LocalDate tripDate);
    List<Trip> findByDriverIdAndTripDate(UUID driverId, LocalDate tripDate);

    @Query("SELECT t FROM Trip t WHERE t.vehicleId = :vehicleId AND t.tripDate = :date AND " +
            "((t.startTime < :endTime AND t.endTime > :startTime) OR (t.startTime IS NOT NULL AND t.endTime IS NULL))")
    List<Trip> findOverlappingTrips(@Param("vehicleId") UUID vehicleId, @Param("date") LocalDate date,
                                     @Param("startTime") OffsetDateTime startTime, @Param("endTime") OffsetDateTime endTime);
}
