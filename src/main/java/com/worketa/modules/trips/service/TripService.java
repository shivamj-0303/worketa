package com.worketa.modules.trips.service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.worketa.common.exception.ApiException;
import com.worketa.common.security.OrganisationContext;
import com.worketa.modules.employees.repository.EmployeeRepository;
import com.worketa.modules.trips.entity.Trip;
import com.worketa.modules.trips.repository.TripRepository;
import com.worketa.modules.vehicles.repository.VehicleRepository;

@Service
public class TripService {

    private final TripRepository tripRepo;
    private final VehicleRepository vehicleRepo;
    private final EmployeeRepository employeeRepo;

    public TripService(TripRepository tripRepo, VehicleRepository vehicleRepo, EmployeeRepository employeeRepo) {
        this.tripRepo = tripRepo;
        this.vehicleRepo = vehicleRepo;
        this.employeeRepo = employeeRepo;
    }

    @Transactional
    public Trip create(Trip trip) {
        var orgId = OrganisationContext.get();
        trip.setOrganisationId(orgId);
        
        // Validate vehicle exists and is active
        var vehicle = vehicleRepo.findByIdAndOrganisationId(trip.getVehicleId(), orgId)
                .orElseThrow(() -> new ApiException("Vehicle not found"));
        if (!vehicle.isActive()) throw new ApiException("Vehicle is inactive");
        
        // Validate driver exists and is active
        var driver = employeeRepo.findByIdAndOrganisationId(trip.getDriverId(), orgId)
                .orElseThrow(() -> new ApiException("Driver not found"));
        if (!driver.isActive()) throw new ApiException("Driver is inactive");
        
        // Validate no overlapping trips for vehicle
        List<Trip> overlapping = tripRepo.findOverlappingTrips(trip.getVehicleId(), trip.getTripDate(),
                trip.getStartTime(), trip.getEndTime() != null ? trip.getEndTime() : OffsetDateTime.now().plusHours(24));
        if (!overlapping.isEmpty()) throw new ApiException("Vehicle has overlapping trip");
        
        // Validate driver not assigned elsewhere same time
        List<Trip> driverTrips = tripRepo.findByDriverIdAndTripDate(trip.getDriverId(), trip.getTripDate());
        for (Trip t : driverTrips) {
            if (trip.getStartTime().isBefore(t.getEndTime() != null ? t.getEndTime() : OffsetDateTime.now().plusHours(24)) &&
                    trip.getEndTime() != null && trip.getEndTime().isAfter(t.getStartTime())) {
                throw new ApiException("Driver has overlapping trip");
            }
        }
        
        return tripRepo.save(trip);
    }

    @Transactional
    public Trip update(UUID id, Trip trip) {
        Trip existing = getById(id);
        existing.setRoute(trip.getRoute());
        existing.setTripDate(trip.getTripDate());
        existing.setStartTime(trip.getStartTime());
        existing.setEndTime(trip.getEndTime());
        existing.setDriverId(trip.getDriverId());
        existing.setVehicleId(trip.getVehicleId());
        return tripRepo.save(existing);
    }

    @Transactional
    public void delete(UUID id) {
        tripRepo.deleteById(id);
    }

    public List<Trip> listByOrg() {
        return tripRepo.findByOrganisationId(OrganisationContext.get());
    }

    public Trip getById(UUID id) {
        return tripRepo.findByIdAndOrganisationId(id, OrganisationContext.get())
                .orElseThrow(() -> new ApiException("Trip not found"));
    }
}
