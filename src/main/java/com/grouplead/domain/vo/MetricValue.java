package com.grouplead.domain.vo;

import com.grouplead.domain.enums.MetricType;

import java.time.LocalDateTime;

public record MetricValue(
        MetricType type,
        String name,
        Double value,
        String unit,
        LocalDateTime timestamp,
        Double previousValue,
        Double changePercent
) {
    public MetricValue(MetricType type, String name, Double value, String unit, LocalDateTime timestamp) {
        this(type, name, value, unit, timestamp, null, null);
    }

    public static MetricValue withChange(MetricType type, String name, Double value, String unit,
                                         LocalDateTime timestamp, Double previousValue) {
        Double changePercent = null;
        if (previousValue != null && previousValue != 0) {
            changePercent = ((value - previousValue) / previousValue) * 100;
        }
        return new MetricValue(type, name, value, unit, timestamp, previousValue, changePercent);
    }

    public boolean hasImproved() {
        return changePercent != null && changePercent > 0;
    }

    public boolean hasDeclined() {
        return changePercent != null && changePercent < 0;
    }

    public String getTrend() {
        if (changePercent == null) return "stable";
        if (changePercent > 5) return "up";
        if (changePercent < -5) return "down";
        return "stable";
    }
}
