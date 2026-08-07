package com.worketa.modules.health.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.worketa.common.response.ApiResponse;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    @GetMapping
    public ResponseEntity<ApiResponse<HealthStatus>> health() {
        HealthStatus status = HealthStatus.builder()
                .status("UP")
                .message("WORKETA Transport Management System is running")
                .version("1.0.0")
                .timestamp(System.currentTimeMillis())
                .build();
        return ResponseEntity.ok(ApiResponse.ok("Service is healthy", status));
    }

    public static class HealthStatus {
        private String status;
        private String message;
        private String version;
        private long timestamp;

        public HealthStatus(String status, String message, String version, long timestamp) {
            this.status = status;
            this.message = message;
            this.version = version;
            this.timestamp = timestamp;
        }

        public static HealthStatusBuilder builder() {
            return new HealthStatusBuilder();
        }

        public String getStatus() {
            return status;
        }

        public String getMessage() {
            return message;
        }

        public String getVersion() {
            return version;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public static class HealthStatusBuilder {
            private String status;
            private String message;
            private String version;
            private long timestamp;

            public HealthStatusBuilder status(String status) {
                this.status = status;
                return this;
            }

            public HealthStatusBuilder message(String message) {
                this.message = message;
                return this;
            }

            public HealthStatusBuilder version(String version) {
                this.version = version;
                return this;
            }

            public HealthStatusBuilder timestamp(long timestamp) {
                this.timestamp = timestamp;
                return this;
            }

            public HealthStatus build() {
                return new HealthStatus(status, message, version, timestamp);
            }
        }
    }
}
