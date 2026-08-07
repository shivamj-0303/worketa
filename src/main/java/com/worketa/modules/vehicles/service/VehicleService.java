package com.worketa.modules.vehicles.service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.worketa.common.exception.ApiException;
import com.worketa.common.security.OrganisationContext;
import com.worketa.modules.vehicles.entity.Vehicle;
import com.worketa.modules.vehicles.repository.VehicleRepository;

@Service
public class VehicleService {

    private final VehicleRepository repo;

    public VehicleService(VehicleRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public Vehicle create(Vehicle vehicle) {
        if (repo.findByVehicleNumber(vehicle.getVehicleNumber()).isPresent()) {
            throw new ApiException("Vehicle number already exists");
        }
        
        vehicle.setOrganisationId(OrganisationContext.get());
        vehicle.setActiveFrom(LocalDate.now());
        return repo.save(vehicle);
    }

    @Transactional
    public Vehicle update(UUID id, Vehicle vehicle) {
        Vehicle existing = getById(id);
        existing.setVehicleNumber(vehicle.getVehicleNumber());
        existing.setType(vehicle.getType());
        existing.setCapacity(vehicle.getCapacity());
        existing.setActive(vehicle.isActive());
        return repo.save(existing);
    }

    @Transactional
    public void delete(UUID id) {
        repo.deleteById(id);
    }

    @Transactional
    public Vehicle markInactive(UUID vehicleId) {
        var vehicle = repo.findByIdAndOrganisationId(vehicleId, OrganisationContext.get())
                .orElseThrow(() -> new ApiException("Vehicle not found"));
        
        vehicle.setActive(false);
        vehicle.setInactiveFrom(LocalDate.now());
        return repo.save(vehicle);
    }

    public List<Vehicle> listByOrg() {
        return repo.findByOrganisationId(OrganisationContext.get());
    }

    public Vehicle getById(UUID id) {
        return repo.findByIdAndOrganisationId(id, OrganisationContext.get())
                .orElseThrow(() -> new ApiException("Vehicle not found"));
    }
}
