package com.worketa.modules.attendance.service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.worketa.common.exception.ApiException;
import com.worketa.common.security.OrganisationContext;
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
        attendance.setOrganisationId(OrganisationContext.get());
        return repo.save(attendance);
    }

    @Transactional
    public Attendance updateAttendance(UUID id, Attendance attendance) {
        Attendance existing = getById(id);
        existing.setType(attendance.getType());
        existing.setCheckinTime(attendance.getCheckinTime());
        existing.setCheckoutTime(attendance.getCheckoutTime());
        return repo.save(existing);
    }

    @Transactional
    public void delete(UUID id) {
        repo.deleteById(id);
    }

    public Attendance getById(UUID id) {
        return repo.findById(id)
                .orElseThrow(() -> new ApiException("Attendance record not found"));
    }

    public Attendance getByEmployeeAndDate(UUID empId, LocalDate date) {
        return repo.findByEmployeeIdAndAttendanceDate(empId, date)
                .orElseThrow(() -> new ApiException("Attendance record not found"));
    }

    public List<Attendance> listByDate(LocalDate date) {
        return repo.findByAttendanceDateAndOrganisationId(date, OrganisationContext.get());
    }

    public List<Attendance> listByOrg() {
        return repo.findByOrganisationId(OrganisationContext.get());
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
}
