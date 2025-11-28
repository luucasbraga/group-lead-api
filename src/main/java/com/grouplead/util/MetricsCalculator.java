package com.grouplead.util;

import java.util.Collections;
import java.util.List;

public final class MetricsCalculator {

    private MetricsCalculator() {
        // Utility class
    }

    public static double calculateAverage(List<Double> values) {
        if (values == null || values.isEmpty()) {
            return 0.0;
        }
        return values.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    }

    public static double calculateMedian(List<Double> values) {
        if (values == null || values.isEmpty()) {
            return 0.0;
        }

        List<Double> sorted = values.stream().sorted().toList();
        int size = sorted.size();

        if (size % 2 == 0) {
            return (sorted.get(size / 2 - 1) + sorted.get(size / 2)) / 2.0;
        }
        return sorted.get(size / 2);
    }

    public static double calculatePercentile(List<Double> values, int percentile) {
        if (values == null || values.isEmpty()) {
            return 0.0;
        }

        List<Double> sorted = values.stream().sorted().toList();
        int index = (int) Math.ceil(percentile / 100.0 * sorted.size()) - 1;
        index = Math.max(0, Math.min(index, sorted.size() - 1));

        return sorted.get(index);
    }

    public static double calculateStandardDeviation(List<Double> values) {
        if (values == null || values.size() < 2) {
            return 0.0;
        }

        double mean = calculateAverage(values);
        double sumSquaredDiff = values.stream()
                .mapToDouble(v -> Math.pow(v - mean, 2))
                .sum();

        return Math.sqrt(sumSquaredDiff / (values.size() - 1));
    }

    public static double calculateVariance(List<Double> values) {
        double stdDev = calculateStandardDeviation(values);
        return stdDev * stdDev;
    }

    public static double calculatePercentageChange(double oldValue, double newValue) {
        if (oldValue == 0) {
            return newValue == 0 ? 0 : 100;
        }
        return ((newValue - oldValue) / oldValue) * 100;
    }

    public static double calculateGrowthRate(List<Double> values) {
        if (values == null || values.size() < 2) {
            return 0.0;
        }

        double first = values.getFirst();
        double last = values.getLast();

        if (first == 0) {
            return 0;
        }

        return ((last - first) / first) * 100;
    }

    public static double calculateMin(List<Double> values) {
        if (values == null || values.isEmpty()) {
            return 0.0;
        }
        return Collections.min(values);
    }

    public static double calculateMax(List<Double> values) {
        if (values == null || values.isEmpty()) {
            return 0.0;
        }
        return Collections.max(values);
    }

    public static double calculateSum(List<Double> values) {
        if (values == null || values.isEmpty()) {
            return 0.0;
        }
        return values.stream().mapToDouble(Double::doubleValue).sum();
    }
}
