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

        when(repo.save(any(Attendance.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Attendance saved = service.markAttendance(attendance);

        assertNotNull(saved);
        assertEquals(AttendanceStatus.PENDING, saved.getStatus());
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
}
