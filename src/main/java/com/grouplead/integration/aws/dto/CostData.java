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
public class CostData {

    private String serviceName;
    private String tagKey;
    private String tagValue;
    private double totalAmount;
    private String currency;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<DailyCost> dailyCosts;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyCost {
        private LocalDate date;
        private double amount;
        private String currency;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResourceCost {
        private String service;
        private String usageType;
        private double amount;
        private String currency;
    }
}
