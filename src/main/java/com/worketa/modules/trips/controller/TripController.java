package com.worketa.modules.trips.controller;

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
import com.worketa.modules.trips.entity.Trip;
import com.worketa.modules.trips.service.TripService;

@RestController
@RequestMapping("/api/v1/trips")
public class TripController {

    private final TripService service;

    public TripController(TripService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Trip>> create(@RequestBody Trip trip) {
        Trip created = service.create(trip);
        return ResponseEntity.ok(ApiResponse.ok("Trip created", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Trip>> update(
            @PathVariable UUID id,
            @RequestBody Trip trip) {
        Trip updated = service.update(id, trip);
        return ResponseEntity.ok(ApiResponse.ok("Trip updated", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Trip deleted", null));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Trip>>> list() {
        List<Trip> trips = service.listByOrg();
        return ResponseEntity.ok(ApiResponse.ok("Trips retrieved", trips));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Trip>> getById(@PathVariable UUID id) {
        Trip trip = service.getById(id);
        return ResponseEntity.ok(ApiResponse.ok("Trip retrieved", trip));
    }
}
