package com.worketa.modules.attendance.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.worketa.modules.attendance.entity.Attendance;

public interface AttendanceRepository extends JpaRepository<Attendance, UUID> {
    List<Attendance> findByOrganisationId(UUID organisationId);
    List<Attendance> findByAttendanceDateAndOrganisationId(LocalDate date, UUID organisationId);
        Optional<Attendance> findByEmployeeIdAndOrganisationIdAndAttendanceDateAndType(
            UUID employeeId,
            UUID organisationId,
            LocalDate date,
            Attendance.AttendanceType type
        );
    List<Attendance> findByOrganisationIdAndStatus(UUID organisationId, Attendance.AttendanceStatus status);
    List<Attendance> findByEmployeeIdAndOrganisationIdAndStatusOrderByAttendanceDateDesc(
        UUID employeeId,
        UUID organisationId,
        Attendance.AttendanceStatus status
    );
    List<Attendance> findByEmployeeIdAndOrganisationIdAndAttendanceDateBetweenOrderByAttendanceDateAsc(
        UUID employeeId,
        UUID organisationId,
        LocalDate start,
        LocalDate end
    );
}
