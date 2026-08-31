package com.worketa.common.time;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public final class WorketaClock {

    private static final LocalTime BUSINESS_DAY_START = LocalTime.of(3, 0);

    private WorketaClock() {
    }

    public static LocalDate businessDate() {
        LocalDateTime now = LocalDateTime.now();
        return now.toLocalTime().isBefore(BUSINESS_DAY_START)
                ? now.toLocalDate().minusDays(1)
                : now.toLocalDate();
    }
}