package com.worketa.modules.vehicles.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.worketa.common.response.ApiResponse;
import com.worketa.modules.vehicles.entity.Vehicle;
import com.worketa.modules.vehicles.service.VehicleService;

@RestController
@RequestMapping("/api/v1/vehicles")
public class VehicleController {

    private final VehicleService service;

    public VehicleController(VehicleService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Vehicle>> create(@RequestBody Vehicle vehicle) {
        Vehicle created = service.create(vehicle);
        return ResponseEntity.ok(ApiResponse.ok("Vehicle created", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Vehicle>> update(
            @PathVariable UUID id,
            @RequestBody Vehicle vehicle) {
        Vehicle updated = service.update(id, vehicle);
        return ResponseEntity.ok(ApiResponse.ok("Vehicle updated", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Vehicle deleted", null));
    }

    @PostMapping("/{id}/mark-inactive")
    public ResponseEntity<ApiResponse<Vehicle>> markInactive(@PathVariable UUID id) {
        Vehicle vehicle = service.markInactive(id);
        return ResponseEntity.ok(ApiResponse.ok("Vehicle marked inactive", vehicle));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Vehicle>>> list() {
        List<Vehicle> vehicles = service.listByOrg();
        return ResponseEntity.ok(ApiResponse.ok("Vehicles retrieved", vehicles));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Vehicle>> getById(@PathVariable UUID id) {
        Vehicle vehicle = service.getById(id);
        return ResponseEntity.ok(ApiResponse.ok("Vehicle retrieved", vehicle));
    }
}
