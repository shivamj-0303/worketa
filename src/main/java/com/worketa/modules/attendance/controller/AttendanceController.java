package com.worketa.modules.attendance.controller;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.worketa.common.response.ApiResponse;
import com.worketa.modules.attendance.entity.Attendance;
import com.worketa.modules.attendance.service.AttendanceService;

@RestController
@RequestMapping("/api/v1/attendance")
public class AttendanceController {

    private final AttendanceService service;

    public AttendanceController(AttendanceService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Attendance>> markAttendance(@RequestBody Attendance attendance) {
        Attendance record = service.markAttendance(attendance);
        return ResponseEntity.ok(ApiResponse.ok("Attendance marked", record));
    }

    @PostMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Attendance>> markAdminAttendance(@RequestBody Attendance attendance) {
        Attendance record = service.markAdminAttendance(attendance);
        return ResponseEntity.ok(ApiResponse.ok("Admin attendance marked", record));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Attendance>> updateAttendance(
            @PathVariable UUID id,
            @RequestBody Attendance attendance) {
        Attendance record = service.updateAttendance(id, attendance);
        return ResponseEntity.ok(ApiResponse.ok("Attendance updated", record));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Attendance>>> listByDate(
            @RequestParam(required = false) LocalDate date) {
        List<Attendance> records = date != null
                ? service.listByDate(date)
                : service.listByOrg();
        return ResponseEntity.ok(ApiResponse.ok("Attendance records retrieved", records));
    }

    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<Attendance>>> listPending() {
        return ResponseEntity.ok(ApiResponse.ok("Pending attendance requests retrieved", service.listPendingByOrg()));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Attendance>> approve(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Attendance approved", service.approveAttendance(id)));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Attendance>> reject(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Attendance rejected", service.rejectAttendance(id)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Attendance>> getById(@PathVariable UUID id) {
        Attendance record = service.getById(id);
        return ResponseEntity.ok(ApiResponse.ok("Attendance record retrieved", record));
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<ApiResponse<List<Attendance>>> listByEmployeeAndMonth(
            @PathVariable UUID employeeId,
            @RequestParam(required = false) String month) {
        YearMonth yearMonth = month != null ? YearMonth.parse(month) : YearMonth.now();
        List<Attendance> records = service.listByEmployeeAndMonth(employeeId, yearMonth);
        return ResponseEntity.ok(ApiResponse.ok("Employee attendance retrieved", records));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Attendance record deleted", null));
    }
}
