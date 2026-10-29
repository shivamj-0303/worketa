package com.worketa.modules.extravehicles.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
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
import com.worketa.modules.extravehicles.entity.ExtraVehicle;
import com.worketa.modules.extravehicles.service.ExtraVehicleService;

@RestController
@RequestMapping("/api/v1/extra-vehicles")
public class ExtraVehicleController {

    private final ExtraVehicleService service;

    public ExtraVehicleController(ExtraVehicleService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ExtraVehicle>>> list() {
        return ResponseEntity.ok(ApiResponse.ok("Extra vehicles retrieved", service.listByOrganisation()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ExtraVehicle>> create(@RequestBody ExtraVehicle extraVehicle) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Extra vehicle created", service.create(extraVehicle)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ExtraVehicle>> update(
            @PathVariable UUID id, @RequestBody ExtraVehicle extraVehicle) {
        return ResponseEntity.ok(ApiResponse.ok("Extra vehicle updated", service.update(id, extraVehicle)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Extra vehicle deleted", null));
    }
}