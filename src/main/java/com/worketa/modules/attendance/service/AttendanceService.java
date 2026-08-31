package com.worketa.modules.attendance.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.worketa.common.exception.ApiException;
import com.worketa.common.security.OrganisationContext;
import com.worketa.common.time.WorketaClock;
import com.worketa.modules.attendance.entity.Attendance;
import com.worketa.modules.attendance.repository.AttendanceRepository;

@Service
public class AttendanceService {

    private final AttendanceRepository repo;

    public AttendanceService(AttendanceRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public Attendance markAttendance(Attendance attendance) {
        UUID organisationId = OrganisationContext.get();
        attendance.setOrganisationId(organisationId);
        attendance.setAttendanceDate(WorketaClock.businessDate());
        if (attendance.getEmployeeId() == null || attendance.getType() == null) {
            throw new ApiException("Employee and attendance type are required");
        }
        if (repo.findByEmployeeIdAndOrganisationIdAndAttendanceDateAndType(
                attendance.getEmployeeId(), organisationId, attendance.getAttendanceDate(), attendance.getType()).isPresent()) {
            throw new ApiException("This attendance request already exists for this date");
        }
        LocalTime currentTime = LocalTime.now();
        boolean doubleWindowOpen = !currentTime.isBefore(LocalTime.of(17, 0))
            || currentTime.isBefore(LocalTime.of(2, 0));
        if (attendance.getType() == Attendance.AttendanceType.WORKED_DOUBLE && !doubleWindowOpen) {
            throw new ApiException("Double attendance can only be requested between 5:00 PM and 2:00 AM");
        }
        attendance.setStatus(Attendance.AttendanceStatus.PENDING);
        return repo.save(attendance);
    }

    @Transactional
    public Attendance approveAttendance(UUID id) {
        Attendance existing = getById(id);
        existing.setStatus(Attendance.AttendanceStatus.APPROVED);
        return repo.save(existing);
    }

    @Transactional
    public Attendance rejectAttendance(UUID id) {
        Attendance existing = getById(id);
        existing.setStatus(Attendance.AttendanceStatus.REJECTED);
        return repo.save(existing);
    }

    @Transactional
    public Attendance updateAttendance(UUID id, Attendance attendance) {
        Attendance existing = getById(id);
        if (existing.getStatus() != Attendance.AttendanceStatus.PENDING) {
            throw new ApiException("Only pending attendance requests can be changed");
        }
        existing.setType(attendance.getType());
        existing.setStatus(Attendance.AttendanceStatus.PENDING);
        existing.setCheckinTime(attendance.getCheckinTime());
        existing.setCheckoutTime(attendance.getCheckoutTime());
        return repo.save(existing);
    }

    @Transactional
    public void delete(UUID id) {
        repo.deleteById(id);
    }

    public Attendance getById(UUID id) {
        return repo.findById(id).filter(record -> record.getOrganisationId().equals(OrganisationContext.get()))
                .orElseThrow(() -> new ApiException("Attendance record not found"));
    }

    public Attendance getByEmployeeAndDate(UUID empId, LocalDate date) {
        return repo.findByEmployeeIdAndOrganisationIdAndAttendanceDateAndType(empId, OrganisationContext.get(), date, Attendance.AttendanceType.PRESENT)
                .orElseThrow(() -> new ApiException("Attendance record not found"));
    }

    public List<Attendance> listByDate(LocalDate date) {
        return repo.findByAttendanceDateAndOrganisationId(date, OrganisationContext.get());
    }

    public List<Attendance> listByOrg() {
        return repo.findByOrganisationId(OrganisationContext.get());
    }

    public List<Attendance> listPendingByOrg() {
        return repo.findByOrganisationIdAndStatus(OrganisationContext.get(), Attendance.AttendanceStatus.PENDING);
    }

    public List<Attendance> listByEmployeeAndMonth(UUID employeeId, YearMonth month) {
        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();
        return repo.findByEmployeeIdAndOrganisationIdAndAttendanceDateBetweenOrderByAttendanceDateAsc(
                employeeId,
                OrganisationContext.get(),
                start,
                end);
    }

    public List<Attendance> listByEmployeeAndStatus(UUID employeeId, Attendance.AttendanceStatus status) {
        return repo.findByEmployeeIdAndOrganisationIdAndStatusOrderByAttendanceDateDesc(
                employeeId,
                OrganisationContext.get(),
                status
        );
    }
}
