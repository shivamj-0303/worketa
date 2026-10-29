package com.worketa.modules.attendance.service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.worketa.common.security.OrganisationContext;
import com.worketa.modules.attendance.entity.Attendance;
import com.worketa.modules.attendance.entity.Attendance.AttendanceStatus;
import com.worketa.modules.attendance.repository.AttendanceRepository;
import com.worketa.modules.employees.entity.Employee;
import com.worketa.modules.employees.repository.EmployeeRepository;

@ExtendWith(MockitoExtension.class)
class AttendanceServiceTest {

    @Mock
    private AttendanceRepository repo;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private AttendanceService service;

    private UUID organisationId;

    @BeforeEach
    void setUp() {
        organisationId = UUID.randomUUID();
        OrganisationContext.set(organisationId);
    }

    @Test
    void markAttendanceWithoutStatusCreatesPendingRequest() {
        Attendance attendance = new Attendance();
        attendance.setEmployeeId(UUID.randomUUID());
        attendance.setAttendanceDate(LocalDate.now());
        attendance.setType(Attendance.AttendanceType.PRESENT);

        when(repo.findByEmployeeIdAndOrganisationIdAndAttendanceDate(
            any(), any(), any())).thenReturn(List.of());
        when(repo.save(any(Attendance.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Attendance saved = service.markAttendance(attendance);

        assertNotNull(saved);
        assertEquals(AttendanceStatus.PENDING, saved.getStatus());
    }

    @Test
    void markAttendanceAllowsDoubleAfterPresentRequest() {
        Attendance attendance = new Attendance();
        attendance.setEmployeeId(UUID.randomUUID());
        attendance.setType(Attendance.AttendanceType.WORKED_DOUBLE);

        Attendance existing = new Attendance();
        existing.setType(Attendance.AttendanceType.PRESENT);
        existing.setStatus(AttendanceStatus.PENDING);
        when(repo.findByEmployeeIdAndOrganisationIdAndAttendanceDate(any(), any(), any()))
                .thenReturn(List.of(existing));
        when(repo.save(any(Attendance.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Attendance saved = service.markAttendance(attendance);

        assertEquals(Attendance.AttendanceType.WORKED_DOUBLE, saved.getType());
        assertEquals(AttendanceStatus.PENDING, saved.getStatus());
    }

    @Test
    void markAttendanceRejectsDirectDoubleRequestWithoutPresent() {
        Attendance attendance = new Attendance();
        attendance.setEmployeeId(UUID.randomUUID());
        attendance.setType(Attendance.AttendanceType.WORKED_DOUBLE);

        when(repo.findByEmployeeIdAndOrganisationIdAndAttendanceDate(any(), any(), any()))
                .thenReturn(List.of());
        org.junit.jupiter.api.Assertions.assertThrows(
            com.worketa.common.exception.ApiException.class,
            () -> service.markAttendance(attendance));
    }

    @Test
    void markAttendanceRejectsDuplicateDoubleRequestForSameEmployeeAndDate() {
        Attendance attendance = new Attendance();
        attendance.setEmployeeId(UUID.randomUUID());
        attendance.setType(Attendance.AttendanceType.WORKED_DOUBLE);

        Attendance existing = new Attendance();
        existing.setType(Attendance.AttendanceType.WORKED_DOUBLE);
        existing.setStatus(AttendanceStatus.PENDING);
        when(repo.findByEmployeeIdAndOrganisationIdAndAttendanceDate(any(), any(), any()))
                .thenReturn(List.of(existing));

        org.junit.jupiter.api.Assertions.assertThrows(
                com.worketa.common.exception.ConflictException.class,
                () -> service.markAttendance(attendance));
    }

    @Test
    void ensureAbsentAttendanceForEmployeeCreatesAbsentRecordWhenNoAttendanceExists() {
        UUID employeeId = UUID.randomUUID();
        Employee employee = new Employee();
        employee.setId(employeeId);
        employee.setOrganisationId(organisationId);
        employee.setActive(true);
        employee.setJoiningDate(LocalDate.now().minusDays(10));

        LocalDate date = LocalDate.now().minusDays(1);

        when(repo.findByAttendanceDateAndOrganisationId(eq(date), eq(organisationId)))
                .thenReturn(List.of());
        when(repo.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Attendance absent = service.ensureAbsentAttendanceForEmployee(employee, date);

        assertNotNull(absent);
        assertEquals(Attendance.AttendanceType.ABSENT, absent.getType());
        assertEquals(AttendanceStatus.APPROVED, absent.getStatus());
        verify(repo).saveAll(any());
    }

    @Test
    void ensureAbsentAttendanceForEmployeeSkipsSunday() {
        Employee employee = new Employee();
        employee.setId(UUID.randomUUID());
        employee.setOrganisationId(organisationId);

        Attendance absent = service.ensureAbsentAttendanceForEmployee(employee, LocalDate.of(2026, 9, 6));

        org.junit.jupiter.api.Assertions.assertNull(absent);
        org.mockito.Mockito.verifyNoInteractions(repo);
    }

    @Test
    void rejectAttendanceConvertsRequestToApprovedAbsentRecord() {
        UUID attendanceId = UUID.randomUUID();
        Attendance attendance = new Attendance();
        attendance.setId(attendanceId);
        attendance.setOrganisationId(organisationId);
        attendance.setEmployeeId(UUID.randomUUID());
        attendance.setAttendanceDate(LocalDate.now());
        attendance.setType(Attendance.AttendanceType.PRESENT);
        attendance.setStatus(AttendanceStatus.PENDING);

        when(repo.findById(attendanceId)).thenReturn(java.util.Optional.of(attendance));
        when(repo.save(any(Attendance.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Attendance saved = service.rejectAttendance(attendanceId);

        assertEquals(Attendance.AttendanceType.ABSENT, saved.getType());
        assertEquals(AttendanceStatus.APPROVED, saved.getStatus());
        verify(repo).save(attendance);
    }


    @Test
    void adminAttendanceCreatesApprovedAuthoritativeRecordForSelectedDate() {
        UUID employeeId = UUID.randomUUID();
        LocalDate selectedDate = LocalDate.of(2026, 9, 7);
        Attendance request = new Attendance();
        request.setEmployeeId(employeeId);
        request.setAttendanceDate(selectedDate);
        request.setType(Attendance.AttendanceType.PRESENT);

        when(repo.findByEmployeeIdAndOrganisationIdAndAttendanceDate(employeeId, organisationId, selectedDate))
                .thenReturn(List.of());
        when(repo.save(any(Attendance.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Attendance saved = service.markAdminAttendance(request);

        assertEquals(employeeId, saved.getEmployeeId());
        assertEquals(selectedDate, saved.getAttendanceDate());
        assertEquals(Attendance.AttendanceType.PRESENT, saved.getType());
        assertEquals(AttendanceStatus.APPROVED, saved.getStatus());
        verify(repo).save(any(Attendance.class));
    }

    @Test
    void approveDoubleRequiresApprovedPresentAndConvertsPresentToDouble() {
        UUID doubleId = UUID.randomUUID();
        UUID presentId = UUID.randomUUID();
        UUID employeeId = UUID.randomUUID();
        LocalDate date = LocalDate.of(2026, 9, 7);

        Attendance doubleRequest = new Attendance();
        doubleRequest.setId(doubleId);
        doubleRequest.setOrganisationId(organisationId);
        doubleRequest.setEmployeeId(employeeId);
        doubleRequest.setAttendanceDate(date);
        doubleRequest.setType(Attendance.AttendanceType.WORKED_DOUBLE);
        doubleRequest.setStatus(AttendanceStatus.PENDING);

        Attendance present = new Attendance();
        present.setId(presentId);
        present.setOrganisationId(organisationId);
        present.setEmployeeId(employeeId);
        present.setAttendanceDate(date);
        present.setType(Attendance.AttendanceType.PRESENT);
        present.setStatus(AttendanceStatus.APPROVED);

        when(repo.findById(doubleId)).thenReturn(java.util.Optional.of(doubleRequest));
        when(repo.findByEmployeeIdAndOrganisationIdAndAttendanceDate(employeeId, organisationId, date))
                .thenReturn(List.of(present, doubleRequest));
        when(repo.save(any(Attendance.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Attendance saved = service.approveAttendance(doubleId, null);

        assertEquals(Attendance.AttendanceType.WORKED_DOUBLE, saved.getType());
        assertEquals(AttendanceStatus.APPROVED, saved.getStatus());
        verify(repo).delete(doubleRequest);
        verify(repo).flush();
    }

    @Test
    void approveDoubleRejectsWhenPresentIsNotApproved() {
        UUID doubleId = UUID.randomUUID();
        UUID employeeId = UUID.randomUUID();
        LocalDate date = LocalDate.of(2026, 9, 7);

        Attendance doubleRequest = new Attendance();
        doubleRequest.setId(doubleId);
        doubleRequest.setOrganisationId(organisationId);
        doubleRequest.setEmployeeId(employeeId);
        doubleRequest.setAttendanceDate(date);
        doubleRequest.setType(Attendance.AttendanceType.WORKED_DOUBLE);

        Attendance present = new Attendance();
        present.setType(Attendance.AttendanceType.PRESENT);
        present.setStatus(AttendanceStatus.PENDING);

        when(repo.findById(doubleId)).thenReturn(java.util.Optional.of(doubleRequest));
        when(repo.findByEmployeeIdAndOrganisationIdAndAttendanceDate(employeeId, organisationId, date))
                .thenReturn(List.of(present, doubleRequest));

        org.junit.jupiter.api.Assertions.assertThrows(
                com.worketa.common.exception.ApiException.class,
                () -> service.approveAttendance(doubleId, null));
    }

    @Test
    void rejectDoubleKeepsApprovedPresent() {
        UUID doubleId = UUID.randomUUID();
        UUID employeeId = UUID.randomUUID();
        LocalDate date = LocalDate.of(2026, 9, 7);

        Attendance doubleRequest = new Attendance();
        doubleRequest.setId(doubleId);
        doubleRequest.setOrganisationId(organisationId);
        doubleRequest.setEmployeeId(employeeId);
        doubleRequest.setAttendanceDate(date);
        doubleRequest.setType(Attendance.AttendanceType.WORKED_DOUBLE);

        Attendance present = new Attendance();
        present.setType(Attendance.AttendanceType.PRESENT);
        present.setStatus(AttendanceStatus.APPROVED);

        when(repo.findById(doubleId)).thenReturn(java.util.Optional.of(doubleRequest));
        when(repo.findByEmployeeIdAndOrganisationIdAndAttendanceDate(employeeId, organisationId, date))
                .thenReturn(List.of(present, doubleRequest));

        Attendance saved = service.rejectAttendance(doubleId);

        assertEquals(Attendance.AttendanceType.PRESENT, saved.getType());
        assertEquals(AttendanceStatus.APPROVED, saved.getStatus());
        verify(repo).delete(doubleRequest);
    }
}
