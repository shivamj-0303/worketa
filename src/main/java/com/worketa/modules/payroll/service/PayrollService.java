package com.worketa.modules.payroll.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.worketa.common.exception.ApiException;
import com.worketa.common.security.OrganisationContext;
import com.worketa.common.time.WorketaClock;
import com.worketa.modules.advances.entity.Advance;
import com.worketa.modules.advances.repository.AdvanceRepository;
import com.worketa.modules.attendance.entity.Attendance;
import com.worketa.modules.attendance.repository.AttendanceRepository;
import com.worketa.modules.employees.entity.Employee;
import com.worketa.modules.employees.repository.EmployeeRepository;
import com.worketa.modules.payroll.dto.PayrollBillUpdateRequest;
import com.worketa.modules.payroll.dto.PayrollSummaryResponse;
import com.worketa.modules.payroll.entity.Payroll;
import com.worketa.modules.payroll.repository.PayrollRepository;

@Service
public class PayrollService {

    private final PayrollRepository payrollRepository;
    private final EmployeeRepository employeeRepository;
    private final AttendanceRepository attendanceRepository;
    private final AdvanceRepository advanceRepository;
        private final ObjectMapper objectMapper;

    public PayrollService(
            PayrollRepository payrollRepository,
            EmployeeRepository employeeRepository,
            AttendanceRepository attendanceRepository,
            AdvanceRepository advanceRepository,
            ObjectMapper objectMapper) {

        this.payrollRepository = payrollRepository;
        this.employeeRepository = employeeRepository;
        this.attendanceRepository = attendanceRepository;
        this.advanceRepository = advanceRepository;
        this.objectMapper = objectMapper;
    }

    public PayrollSummaryResponse getPayrollSummary(UUID employeeId) {

        Employee employee = employeeRepository
                .findByIdAndOrganisationId(
                        employeeId,
                        OrganisationContext.get())
                .orElseThrow(() -> new ApiException("Employee not found"));

        var latestPayroll =
            payrollRepository
                    .findTopByEmployeeIdAndOrganisationIdOrderByPeriodEndDesc(
                            employeeId,
                            OrganisationContext.get()
                    );

        LocalDate startDate =
                latestPayroll.isPresent()
                        ? latestPayroll.get().getPeriodEnd().plusDays(1)
                        : employee.getJoiningDate();

        LocalDate endDate = WorketaClock.businessDate();

        List<Attendance> attendances =
                                attendanceRepository.findByEmployeeIdAndOrganisationIdAndAttendanceDateBetweenOrderByAttendanceDateAsc(
                        employeeId,
                                                OrganisationContext.get(),
                        startDate,
                        endDate);

        int presentDays = 0;
        int doubledDays = 0;
        int absentDays = 0;
        int requestedPresentDays = 0;
        int requestedDoubledDays = 0;
        int requestedAbsentDays = 0;

        for (Attendance attendance : attendances) {
                        switch (attendance.getType()) {
                                case PRESENT -> requestedPresentDays++;
                                case WORKED_DOUBLE -> requestedDoubledDays++;
                                case ABSENT -> requestedAbsentDays++;
                        }

                        if (attendance.getStatus() != Attendance.AttendanceStatus.APPROVED) {
                                continue;
                        }

                        switch (attendance.getType()) {
                                case PRESENT -> presentDays++;
                                case WORKED_DOUBLE -> doubledDays++;
                                case ABSENT -> absentDays++;
            }
        }

        BigDecimal dailyWage = employee.getDailyWage();

        BigDecimal bonusAmount = BigDecimal.valueOf(500L).multiply(BigDecimal.valueOf(doubledDays));

        BigDecimal grossAmount =
                dailyWage.multiply(BigDecimal.valueOf(presentDays))
                        .add(dailyWage.multiply(BigDecimal.valueOf(doubledDays)))
                        .add(bonusAmount);

        List<Advance> advances =
                advanceRepository
                        .findByEmployeeIdAndOrganisationIdAndStatusAndSettledFalseAndAdvanceDateBetween(
                                employeeId,
                                OrganisationContext.get(),
                                Advance.AdvanceStatus.APPROVED,
                                startDate,
                                endDate);

        BigDecimal advanceDeduction = advances.stream()
                .map(Advance::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal requestedAdvanceAmount = advanceRepository
                .findByEmployeeIdAndOrganisationIdAndSettledFalseAndAdvanceDateBetween(
                        employeeId, OrganisationContext.get(), startDate, endDate)
                .stream()
                .filter(advance -> advance.getStatus() != Advance.AdvanceStatus.REJECTED)
                .map(Advance::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal netAmount =
                grossAmount.subtract(advanceDeduction);

        PayrollSummaryResponse response = new PayrollSummaryResponse();

        response.setEmployeeId(employee.getId());
        response.setEmployeeName(employee.getFullName());

        response.setPeriodStart(startDate);
        response.setPeriodEnd(endDate);

        response.setDailyWage(dailyWage);

        response.setPresentDays(presentDays);
        response.setDoubledDays(doubledDays);
        response.setAbsentDays(absentDays);
        response.setRequestedPresentDays(requestedPresentDays);
        response.setRequestedDoubledDays(requestedDoubledDays);
        response.setRequestedAbsentDays(requestedAbsentDays);

        response.setGrossAmount(grossAmount);
        response.setBonusAmount(bonusAmount);
        response.setAdvanceDeduction(advanceDeduction);
        response.setNetAmount(netAmount);
        response.setRequestedAdvanceAmount(requestedAdvanceAmount);
        response.setRequestedBonusAmount(BigDecimal.valueOf(500L).multiply(BigDecimal.valueOf(requestedDoubledDays)));

        return response;
    }

    @Transactional
    public Payroll settlePayroll(UUID employeeId) {

        PayrollSummaryResponse summary =
                getPayrollSummary(employeeId);

        Payroll payroll = new Payroll();

        payroll.setEmployeeId(employeeId);
        payroll.setOrganisationId(OrganisationContext.get());
        payroll.setEmployeeName(summary.getEmployeeName());
        payroll.setMonth(formatMonth(summary.getPeriodEnd()));
        payroll.setBaseSalary(summary.getGrossAmount());

        payroll.setPeriodStart(summary.getPeriodStart());
        payroll.setPeriodEnd(summary.getPeriodEnd());

        payroll.setDailyWage(summary.getDailyWage());

        payroll.setPresentDays(summary.getPresentDays());
        payroll.setDoubledDays(summary.getDoubledDays());
        payroll.setAbsentDays(summary.getAbsentDays());

        payroll.setGrossAmount(summary.getGrossAmount());
        payroll.setBonusAmount(summary.getBonusAmount());
        payroll.setAdvanceDeduction(summary.getAdvanceDeduction());
        payroll.setNetAmount(summary.getNetAmount());
        payroll.setRemarks(buildRemarks(summary));

        payroll.setSettledAt(LocalDateTime.now());

        List<Advance> advances =
                advanceRepository
                        .findByEmployeeIdAndOrganisationIdAndStatusAndSettledFalseAndAdvanceDateBetween(
                                employeeId,
                                OrganisationContext.get(),
                                Advance.AdvanceStatus.APPROVED,
                                summary.getPeriodStart(),
                                summary.getPeriodEnd());

        payroll.setAdvancesSnapshot(serializeAdvancesSnapshot(advances));

        payroll = payrollRepository.save(payroll);

        for (Advance advance : advances) {
            advance.setSettled(true);
        }

        advanceRepository.saveAll(advances);

        return payroll;
    }

    public List<Payroll> listByEmployee(UUID employeeId) {
                return enrichEmployeeNames(payrollRepository.findByEmployeeId(employeeId));
    }

        public List<Payroll> listHistory() {
                return enrichEmployeeNames(
                                payrollRepository.findByOrganisationIdOrderBySettledAtDesc(OrganisationContext.get())
                );
        }

        public Payroll getHistoryById(UUID id) {
                Payroll payroll = payrollRepository.findByIdAndOrganisationId(id, OrganisationContext.get())
                                .orElseThrow(() -> new ApiException("Payroll bill not found"));

                enrichEmployeeName(payroll);
                return payroll;
        }

        @Transactional
        public Payroll updateHistory(UUID id, PayrollBillUpdateRequest request) {
                Payroll payroll = getHistoryById(id);

                payroll.setEmployeeName(request.getEmployeeName());
                payroll.setPeriodStart(request.getPeriodStart());
                payroll.setPeriodEnd(request.getPeriodEnd());
                payroll.setDailyWage(request.getDailyWage());
                payroll.setPresentDays(request.getPresentDays());
                payroll.setDoubledDays(request.getDoubledDays());
                payroll.setAbsentDays(request.getAbsentDays());
                payroll.setGrossAmount(request.getGrossAmount());
                payroll.setAdvanceDeduction(request.getAdvanceDeduction());
                payroll.setNetAmount(request.getNetAmount());
                payroll.setSettledAt(request.getSettledAt());
                payroll.setAdvancesSnapshot(request.getAdvancesSnapshot());

                return payrollRepository.save(payroll);
        }

    public List<PayrollSummaryResponse> getAllSummaries() {

        List<Employee> employees =
                employeeRepository.findByOrganisationIdAndActiveTrue(
                        OrganisationContext.get());

        return employees.stream()
                .map(employee -> getPayrollSummary(employee.getId()))
                .toList();
    }

    private String serializeAdvancesSnapshot(List<Advance> advances) {
        List<Map<String, Object>> snapshot = advances.stream()
                .map(advance -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("id", advance.getId());
                    item.put("amount", advance.getAmount());
                    item.put("advanceDate", advance.getAdvanceDate());
                    item.put("note", advance.getNote());
                    return item;
                })
                .toList();

        try {
            return objectMapper.writeValueAsString(snapshot);
        } catch (JsonProcessingException exception) {
            return "[]";
        }
    }

    private String formatMonth(LocalDate date) {
        return String.format("%04d-%02d", date.getYear(), date.getMonthValue());
    }

    private String buildRemarks(PayrollSummaryResponse summary) {
        return "Settlement from "
                + summary.getPeriodStart()
                + " to "
                + summary.getPeriodEnd()
                + "; present="
                + summary.getPresentDays()
                + ", double="
                + summary.getDoubledDays()
                + ", absent="
                + summary.getAbsentDays();
        }

        private List<Payroll> enrichEmployeeNames(List<Payroll> payrolls) {
                payrolls.forEach(this::enrichEmployeeName);
                return payrolls;
        }

        private void enrichEmployeeName(Payroll payroll) {
                if (payroll.getEmployeeName() != null && !payroll.getEmployeeName().isBlank()) {
                        return;
                }

                employeeRepository.findByIdAndOrganisationId(payroll.getEmployeeId(), OrganisationContext.get())
                                .ifPresent(employee -> payroll.setEmployeeName(employee.getFullName()));
        }
}
