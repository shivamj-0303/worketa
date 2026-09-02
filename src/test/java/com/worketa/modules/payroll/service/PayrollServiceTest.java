package com.worketa.modules.payroll.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.worketa.common.security.OrganisationContext;
import com.worketa.modules.advances.entity.Advance;
import com.worketa.modules.advances.repository.AdvanceRepository;
import com.worketa.modules.attendance.entity.Attendance;
import com.worketa.modules.attendance.entity.Attendance.AttendanceType;
import com.worketa.modules.attendance.repository.AttendanceRepository;
import com.worketa.modules.employees.entity.Employee;
import com.worketa.modules.employees.repository.EmployeeRepository;
import com.worketa.modules.payroll.entity.Payroll;
import com.worketa.modules.payroll.repository.PayrollRepository;

@ExtendWith(MockitoExtension.class)
class PayrollServiceTest {

    @Mock
    private PayrollRepository payrollRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private AdvanceRepository advanceRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private PayrollService service;

    private UUID organisationId;
    private UUID employeeId;
    private Employee employee;

    @BeforeEach
    void setUp() {
        organisationId = UUID.randomUUID();
        employeeId = UUID.randomUUID();
        OrganisationContext.set(organisationId);

        employee = new Employee();
        employee.setId(employeeId);
        employee.setOrganisationId(organisationId);
        employee.setFullName("Test Employee");
        employee.setDailyWage(new BigDecimal("500.00"));
        employee.setJoiningDate(LocalDate.now().minusDays(10));
    }

    @Test
    void settlePayrollCreatesPersistedBillSnapshot() throws Exception {
        when(employeeRepository.findByIdAndOrganisationId(employeeId, organisationId))
                .thenReturn(Optional.of(employee));
        when(attendanceRepository.findByEmployeeIdAndOrganisationIdAndAttendanceDateBetweenOrderByAttendanceDateAsc(any(), any(), any(), any()))
                .thenReturn(List.of(createAttendance(AttendanceType.PRESENT), createAttendance(AttendanceType.WORKED_DOUBLE)));
            when(advanceRepository.findByEmployeeIdAndOrganisationIdAndStatusAndSettledFalseAndAdvanceDateBetween(any(), any(), any(), any(), any()))
                .thenReturn(List.of(createAdvance()));
            when(advanceRepository.findByEmployeeIdAndOrganisationIdAndSettledFalseAndAdvanceDateBetween(any(), any(), any(), any()))
                .thenReturn(List.of(createAdvance()));
        when(objectMapper.writeValueAsString(anyList())).thenReturn("[]");
        when(payrollRepository.save(any(Payroll.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Payroll payroll = service.settlePayroll(employeeId);

        assertNotNull(payroll);
        assertEquals("Test Employee", payroll.getEmployeeName());
        assertEquals(String.format("%04d-%02d", LocalDate.now().getYear(), LocalDate.now().getMonthValue()), payroll.getMonth());
            // dailyWage: 500, 1 PRESENT day (500) + 1 DOUBLE day (500 bonus only) = 1000
            assertEquals(new BigDecimal("1000.00"), payroll.getBaseSalary());
            assertEquals(new BigDecimal("1000.00"), payroll.getGrossAmount());

        ArgumentCaptor<Payroll> payrollCaptor = ArgumentCaptor.forClass(Payroll.class);
        verify(payrollRepository).save(payrollCaptor.capture());
        assertEquals("Test Employee", payrollCaptor.getValue().getEmployeeName());
        assertEquals(String.format("%04d-%02d", LocalDate.now().getYear(), LocalDate.now().getMonthValue()), payrollCaptor.getValue().getMonth());
        verify(advanceRepository).saveAll(anyList());
    }

    private Attendance createAttendance(AttendanceType type) {
        Attendance attendance = new Attendance();
        attendance.setEmployeeId(employeeId);
        attendance.setAttendanceDate(LocalDate.now());
        attendance.setType(type);
            attendance.setStatus(Attendance.AttendanceStatus.APPROVED);
        return attendance;
    }

    private Advance createAdvance() {
        Advance advance = new Advance();
        advance.setId(UUID.randomUUID());
        advance.setEmployeeId(employeeId);
        advance.setAmount(new BigDecimal("250.00"));
        advance.setAdvanceDate(LocalDate.now());
        return advance;
    }
}