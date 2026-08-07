package com.worketa.modules.dashboard.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.worketa.common.response.ApiResponse;
import com.worketa.modules.dashboard.dto.DashboardStatsResponse;
import com.worketa.modules.dashboard.service.DashboardService;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    private final DashboardService service;

    public DashboardController(
            DashboardService service
    ) {
        this.service = service;
    }

    @GetMapping("/stats")
    public ResponseEntity<
            ApiResponse<DashboardStatsResponse>
            > getStats() {

        DashboardStatsResponse stats =
                service.getStats();

        return ResponseEntity.ok(
            ApiResponse.ok(
                "Dashboard statistics retrieved",
                stats
            )
        );
    }
}