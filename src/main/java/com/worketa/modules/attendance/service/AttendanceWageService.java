package com.worketa.modules.attendance.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import com.worketa.modules.attendance.entity.Attendance;

/**
 * Service for calculating wages based on attendance type.
 * Rules:
 * - PRESENT: 1x daily wage
 * - ABSENT: 0x daily wage (no wage)
 * - WORKED_DOUBLE: normal daily wage plus a fixed double-shift allowance of 500
 */
@Service
public class AttendanceWageService {

    /**
     * Calculate daily wage based on attendance type and employee's daily wage.
     * 
     * @param type the attendance type
     * @param employeeDailyWage the employee's daily wage rate
     * @return the calculated wage for the day
     */
    public BigDecimal calculateWageForDay(Attendance.AttendanceType type, BigDecimal employeeDailyWage) {
        if (employeeDailyWage == null) {
            employeeDailyWage = BigDecimal.ZERO;
        }

        return switch (type) {
            case PRESENT -> employeeDailyWage;
            case ABSENT -> BigDecimal.ZERO;
            case WORKED_DOUBLE -> employeeDailyWage.add(BigDecimal.valueOf(500L));
        };
    }

    /**
     * Calculate daily wage for an Attendance record.
     * 
     * @param attendance the attendance record
     * @param employeeDailyWage the employee's daily wage rate
     * @return the attendance record with wageForDay set
     */
    public Attendance calculateAndSetWage(Attendance attendance, BigDecimal employeeDailyWage) {
        calculateWageForDay(attendance.getType(), employeeDailyWage);
        return attendance;
    }
}
