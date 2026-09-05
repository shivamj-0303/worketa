package com.worketa.modules.attendance.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.worketa.common.exception.ApiException;
import com.worketa.common.security.OrganisationContext;
import com.worketa.common.time.WorketaClock;
import com.worketa.modules.attendance.entity.Attendance;
import com.worketa.modules.attendance.repository.AttendanceRepository;
import com.worketa.modules.employees.entity.Employee;
import com.worketa.modules.employees.repository.EmployeeRepository;

@Service
public class AttendanceService {

    private final AttendanceRepository repo;
    private final EmployeeRepository employeeRepository;

    public AttendanceService(AttendanceRepository repo, EmployeeRepository employeeRepository) {
        this.repo = repo;
        this.employeeRepository = employeeRepository;
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
        LocalTime currentTime = WorketaClock.now().toLocalTime();
        boolean doubleWindowOpen = !currentTime.isBefore(LocalTime.of(17, 0))
            || currentTime.isBefore(LocalTime.of(2, 0));
        if (attendance.getType() == Attendance.AttendanceType.WORKED_DOUBLE && !doubleWindowOpen) {
            throw new ApiException("Double attendance can only be requested between 5:00 PM and 2:00 AM");
        }
        attendance.setStatus(Attendance.AttendanceStatus.PENDING);
        return repo.save(attendance);
    }

    @Scheduled(cron = "0 0 * * * *", zone = "Asia/Kolkata")
    @Transactional
    public void finalizePreviousBusinessDay() {
        LocalTime now = WorketaClock.now().toLocalTime();
        if (now.isBefore(LocalTime.of(3, 0))) {
            return;
        }

        LocalDate date = WorketaClock.businessDate().minusDays(1);
        for (Employee employee : employeeRepository.findAll()) {
            if (!employee.isActive()
                    || employee.getJoiningDate() == null
                    || employee.getJoiningDate().isAfter(date)
                    || (employee.getLeavingDate() != null && employee.getLeavingDate().isBefore(date))) {
                continue;
            }

            ensureAbsentAttendanceForEmployee(employee, date);
        }
    }

    @Transactional
    public Attendance ensureAbsentAttendanceForEmployee(Employee employee, LocalDate date) {
        List<Attendance> records = new ArrayList<>(
                repo.findByAttendanceDateAndOrganisationId(date, employee.getOrganisationId())
                        .stream()
                        .filter(record -> record.getEmployeeId().equals(employee.getId()))
                        .toList()
        );

        boolean approvedAttendanceExists = records.stream()
                .anyMatch(record -> record.getStatus() == Attendance.AttendanceStatus.APPROVED
                        && record.getType() != Attendance.AttendanceType.ABSENT);
        if (approvedAttendanceExists) {
            return null;
        }

        records.stream()
                .filter(record -> record.getStatus() == Attendance.AttendanceStatus.PENDING)
                .forEach(record -> {
                    record.setType(Attendance.AttendanceType.ABSENT);
                    record.setStatus(Attendance.AttendanceStatus.APPROVED);
                    record.setCheckinTime(null);
                    record.setCheckoutTime(null);
                });

        boolean absentExists = records.stream()
                .anyMatch(record -> record.getType() == Attendance.AttendanceType.ABSENT);
        if (absentExists) {
            return repo.saveAll(records).stream()
                    .filter(record -> record.getType() == Attendance.AttendanceType.ABSENT)
                    .findFirst()
                    .orElse(null);
        }

        Attendance absent = new Attendance();
        absent.setOrganisationId(employee.getOrganisationId());
        absent.setEmployeeId(employee.getId());
        absent.setAttendanceDate(date);
        absent.setType(Attendance.AttendanceType.ABSENT);
        absent.setStatus(Attendance.AttendanceStatus.APPROVED);
        records.add(absent);

        repo.saveAll(records);
        return absent;
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
        existing.setType(Attendance.AttendanceType.ABSENT);
        existing.setStatus(Attendance.AttendanceStatus.APPROVED);
        existing.setCheckinTime(null);
        existing.setCheckoutTime(null);
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
