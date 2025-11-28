package com.grouplead.dto.request;

import com.grouplead.domain.vo.DateRange;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record DateRangeRequest(
        @NotNull(message = "Start date is required")
        LocalDate startDate,

        @NotNull(message = "End date is required")
        LocalDate endDate
) {
    public DateRange toDateRange() {
        return DateRange.of(startDate, endDate);
    }
}
