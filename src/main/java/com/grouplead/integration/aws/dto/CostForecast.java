package com.grouplead.integration.aws.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CostForecast {

    private double totalForecastedAmount;
    private String currency;
    private List<ForecastPeriod> forecastPeriods;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ForecastPeriod {
        private LocalDate startDate;
        private LocalDate endDate;
        private double meanValue;
        private double predictionIntervalLowerBound;
        private double predictionIntervalUpperBound;
    }
}
