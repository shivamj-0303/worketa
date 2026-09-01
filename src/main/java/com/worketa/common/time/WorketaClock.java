package com.worketa.common.time;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public final class WorketaClock {

    public static final ZoneId INDIA_ZONE = ZoneId.of("Asia/Kolkata");
    private static final LocalTime BUSINESS_DAY_START = LocalTime.of(3, 0);

    private WorketaClock() {
    }

    public static LocalDate businessDate() {
        LocalDateTime now = ZonedDateTime.now(INDIA_ZONE).toLocalDateTime();
        return now.toLocalTime().isBefore(BUSINESS_DAY_START)
                ? now.toLocalDate().minusDays(1)
                : now.toLocalDate();
    }

    public static ZonedDateTime now() {
        return ZonedDateTime.now(INDIA_ZONE);
    }
}