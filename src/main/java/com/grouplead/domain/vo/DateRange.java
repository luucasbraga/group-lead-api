package com.grouplead.domain.vo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public record DateRange(
        LocalDateTime start,
        LocalDateTime end
) {
    public DateRange {
        if (start != null && end != null && start.isAfter(end)) {
            throw new IllegalArgumentException("Start date must be before end date");
        }
    }

    public static DateRange of(LocalDate startDate, LocalDate endDate) {
        return new DateRange(
                startDate.atStartOfDay(),
                endDate.atTime(23, 59, 59)
        );
    }

    public static DateRange lastDays(int days) {
        LocalDateTime now = LocalDateTime.now();
        return new DateRange(now.minusDays(days), now);
    }

    public static DateRange lastWeeks(int weeks) {
        LocalDateTime now = LocalDateTime.now();
        return new DateRange(now.minusWeeks(weeks), now);
    }

    public static DateRange lastMonths(int months) {
        LocalDateTime now = LocalDateTime.now();
        return new DateRange(now.minusMonths(months), now);
    }

    public long getDays() {
        return ChronoUnit.DAYS.between(start, end);
    }

    public long getHours() {
        return ChronoUnit.HOURS.between(start, end);
    }

    public boolean contains(LocalDateTime dateTime) {
        return !dateTime.isBefore(start) && !dateTime.isAfter(end);
    }
}
