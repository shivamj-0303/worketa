package com.worketa.modules.attendance.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.math.BigDecimal;

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
        if (attendance.getAttendanceDate().getDayOfWeek() == java.time.DayOfWeek.SUNDAY) {
            throw new ApiException("Attendance is not recorded on Sundays");
        }
        if (attendance.getEmployeeId() == null || attendance.getType() == null) {
            throw new ApiException("Employee and attendance type are required");
        }
        List<Attendance> records = repo.findByEmployeeIdAndOrganisationIdAndAttendanceDate(
                attendance.getEmployeeId(), organisationId, attendance.getAttendanceDate());
        if (records.stream().anyMatch(record -> record.getType() == attendance.getType())) {
            throw new ApiException("This attendance type has already been submitted for this date");
        }
        if (attendance.getType() == Attendance.AttendanceType.ABSENT || records.stream()
                .anyMatch(record -> record.getType() == Attendance.AttendanceType.ABSENT)) {
            throw new ApiException("Absent attendance cannot be combined with another attendance type");
        }
        if (attendance.getType() == Attendance.AttendanceType.WORKED_DOUBLE && records.stream()
                .noneMatch(record -> record.getType() == Attendance.AttendanceType.PRESENT
                        && record.getStatus() != Attendance.AttendanceStatus.REJECTED)) {
            throw new ApiException("A present attendance request is required before requesting double attendance");
        }
        LocalTime currentTime = WorketaClock.now().toLocalTime();
        boolean doubleWindowOpen = !currentTime.isBefore(LocalTime.of(17, 0))
            || currentTime.isBefore(LocalTime.of(2, 0));
        if (attendance.getType() == Attendance.AttendanceType.WORKED_DOUBLE && !doubleWindowOpen) {
            throw new ApiException("Double attendance can only be requested between 5:00 PM and 2:00 AM");
        }
        attendance.setStatus(Attendance.AttendanceStatus.PENDING);
        attendance.setBonusAmount(BigDecimal.ZERO);
        return repo.save(attendance);
    }

    @Transactional
    public Attendance markAdminAttendance(Attendance attendance) {
        UUID organisationId = OrganisationContext.get();
        if (attendance.getEmployeeId() == null
                || attendance.getAttendanceDate() == null
                || attendance.getType() == null) {
            throw new ApiException("Employee, attendance date and attendance type are required");
        }
        if (attendance.getAttendanceDate().getDayOfWeek() == java.time.DayOfWeek.SUNDAY) {
            throw new ApiException("Attendance is not recorded on Sundays");
        }

        List<Attendance> records = repo.findByEmployeeIdAndOrganisationIdAndAttendanceDate(
            attendance.getEmployeeId(), organisationId, attendance.getAttendanceDate());

        Attendance authoritative = records.isEmpty() ? new Attendance() : records.get(0);
        authoritative.setOrganisationId(organisationId);
        authoritative.setEmployeeId(attendance.getEmployeeId());
        authoritative.setAttendanceDate(attendance.getAttendanceDate());
        authoritative.setType(attendance.getType());
        authoritative.setStatus(Attendance.AttendanceStatus.APPROVED);
        authoritative.setBonusAmount(attendance.getType() == Attendance.AttendanceType.WORKED_DOUBLE
            ? defaultBonus(attendance.getBonusAmount()) : BigDecimal.ZERO);
        authoritative.setCheckinTime(attendance.getCheckinTime());
        authoritative.setCheckoutTime(attendance.getCheckoutTime());

        if (records.size() > 1) {
            repo.deleteAll(records.stream()
                    .filter(record -> !record.getId().equals(authoritative.getId()))
                    .toList());
        }

        return repo.save(authoritative);
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
        if (date.getDayOfWeek() == java.time.DayOfWeek.SUNDAY) {
            return null;
        }

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
        return approveAttendance(id, null);
    }

    @Transactional
    public Attendance approveAttendance(UUID id, BigDecimal bonusAmount) {
        Attendance existing = getById(id);
        if (existing.getType() == Attendance.AttendanceType.WORKED_DOUBLE) {
            List<Attendance> sameDayRecords = repo.findByEmployeeIdAndOrganisationIdAndAttendanceDate(
                    existing.getEmployeeId(), existing.getOrganisationId(), existing.getAttendanceDate());
            Attendance present = sameDayRecords.stream()
                    .filter(record -> !record.getId().equals(existing.getId()))
                    .filter(record -> record.getType() == Attendance.AttendanceType.PRESENT)
                    .filter(record -> record.getStatus() != Attendance.AttendanceStatus.REJECTED)
                    .findFirst()
                    .orElse(null);
            if (present != null) {
                repo.delete(existing);
                repo.flush();
                present.setType(Attendance.AttendanceType.WORKED_DOUBLE);
                present.setBonusAmount(defaultBonus(bonusAmount));
                present.setStatus(Attendance.AttendanceStatus.APPROVED);
                return repo.save(present);
            }
            existing.setBonusAmount(defaultBonus(bonusAmount));
        }
        existing.setStatus(Attendance.AttendanceStatus.APPROVED);
        return repo.save(existing);
    }

    private BigDecimal defaultBonus(BigDecimal bonusAmount) {
        return bonusAmount == null ? BigDecimal.valueOf(500L) : bonusAmount;
    }

    @Transactional
    public Attendance rejectAttendance(UUID id) {
        Attendance existing = getById(id);
        if (existing.getType() == Attendance.AttendanceType.WORKED_DOUBLE) {
            List<Attendance> sameDayRecords = repo.findByEmployeeIdAndOrganisationIdAndAttendanceDate(
                    existing.getEmployeeId(), existing.getOrganisationId(), existing.getAttendanceDate());
            Attendance present = sameDayRecords.stream()
                    .filter(record -> !record.getId().equals(existing.getId()))
                    .filter(record -> record.getType() == Attendance.AttendanceType.PRESENT)
                    .filter(record -> record.getStatus() != Attendance.AttendanceStatus.REJECTED)
                    .findFirst()
                    .orElseThrow(() -> new ApiException("Present attendance not found for double request"));
            repo.delete(existing);
            return present;
        }
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
                .filter(record -> record.getAttendanceDate().getDayOfWeek() != java.time.DayOfWeek.SUNDAY)
                .orElseThrow(() -> new ApiException("Attendance record not found"));
    }

    public Attendance getByEmployeeAndDate(UUID empId, LocalDate date) {
        if (date.getDayOfWeek() == java.time.DayOfWeek.SUNDAY) {
            throw new ApiException("Attendance is not recorded on Sundays");
        }
        return repo.findByEmployeeIdAndOrganisationIdAndAttendanceDateAndType(empId, OrganisationContext.get(), date, Attendance.AttendanceType.PRESENT)
                .orElseThrow(() -> new ApiException("Attendance record not found"));
    }

    public List<Attendance> listByDate(LocalDate date) {
        if (date.getDayOfWeek() == java.time.DayOfWeek.SUNDAY) {
            return List.of();
        }
        return repo.findByAttendanceDateAndOrganisationId(date, OrganisationContext.get());
    }

    public List<Attendance> listByOrg() {
        return repo.findByOrganisationId(OrganisationContext.get()).stream()
                .filter(record -> record.getAttendanceDate().getDayOfWeek() != java.time.DayOfWeek.SUNDAY)
                .toList();
    }

    public List<Attendance> listByOrganisationAndMonth(YearMonth month) {
        return repo.findByOrganisationIdAndAttendanceDateBetweenOrderByAttendanceDateAsc(
            OrganisationContext.get(), month.atDay(1), month.atEndOfMonth()).stream()
            .filter(record -> record.getAttendanceDate().getDayOfWeek() != java.time.DayOfWeek.SUNDAY)
            .toList();
    }

    public List<Attendance> listPendingByOrg() {
        return repo.findByOrganisationIdAndStatus(OrganisationContext.get(), Attendance.AttendanceStatus.PENDING).stream()
            .filter(record -> record.getAttendanceDate().getDayOfWeek() != java.time.DayOfWeek.SUNDAY)
            .toList();
    }

    public List<Attendance> listByEmployeeAndMonth(UUID employeeId, YearMonth month) {
        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();
        return repo.findByEmployeeIdAndOrganisationIdAndAttendanceDateBetweenOrderByAttendanceDateAsc(
                employeeId,
                OrganisationContext.get(),
                start,
            end).stream()
            .filter(record -> record.getAttendanceDate().getDayOfWeek() != java.time.DayOfWeek.SUNDAY)
            .toList();
    }

    public List<Attendance> listByEmployeeAndStatus(UUID employeeId, Attendance.AttendanceStatus status) {
        return repo.findByEmployeeIdAndOrganisationIdAndStatusOrderByAttendanceDateDesc(
                employeeId,
            OrganisationContext.get(),
            status
        ).stream()
            .filter(record -> record.getAttendanceDate().getDayOfWeek() != java.time.DayOfWeek.SUNDAY)
            .toList();
    }
}
