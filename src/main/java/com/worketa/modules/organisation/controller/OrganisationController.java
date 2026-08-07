package com.worketa.modules.organisation.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.worketa.common.response.ApiResponse;
import com.worketa.modules.organisation.dto.OrganisationCreateRequest;
import com.worketa.modules.organisation.entity.Organisation;
import com.worketa.modules.organisation.service.OrganisationService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/organisations")
public class OrganisationController {

    private final OrganisationService service;

    public OrganisationController(OrganisationService service) {
        this.service = service;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Organisation>> register(
            @Valid @RequestBody OrganisationCreateRequest req) {

        Organisation org = service.register(req);

        return ResponseEntity.ok(
                ApiResponse.ok("Organisation created", org)
        );
    }
}
