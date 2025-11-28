package com.grouplead.integration.aws.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CloudWatchMetric {

    private String namespace;
    private String metricName;
    private String dimensionName;
    private String dimensionValue;
    private double averageValue;
    private double maximumValue;
    private double minimumValue;
    private String unit;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private int dataPointCount;
}
