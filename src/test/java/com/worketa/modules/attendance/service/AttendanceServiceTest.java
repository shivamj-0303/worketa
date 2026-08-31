package com.worketa.modules.attendance.service;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.worketa.common.security.OrganisationContext;
import com.worketa.modules.attendance.entity.Attendance;
import com.worketa.modules.attendance.entity.Attendance.AttendanceStatus;
import com.worketa.modules.attendance.repository.AttendanceRepository;

@ExtendWith(MockitoExtension.class)
class AttendanceServiceTest {

    @Mock
    private AttendanceRepository repo;

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
}
