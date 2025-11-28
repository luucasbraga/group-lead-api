package com.grouplead.dto.response;

import com.grouplead.domain.enums.MetricType;

import java.time.LocalDateTime;
import java.util.List;

public record TimeSeriesResponse(
        MetricType metricType,
        String granularity,
        List<DataPoint> data,
        Statistics statistics
) {
    public record DataPoint(
            LocalDateTime timestamp,
            Double value
    ) {}

    public record Statistics(
            Double min,
            Double max,
            Double average,
            Double median,
            Double p95,
            Double p99
    ) {}
}
