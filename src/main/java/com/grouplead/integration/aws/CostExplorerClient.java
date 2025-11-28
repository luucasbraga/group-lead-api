package com.grouplead.integration.aws;

import com.grouplead.config.IntegrationProperties;
import com.grouplead.exception.IntegrationException;
import com.grouplead.integration.aws.dto.CostData;
import com.grouplead.integration.aws.dto.CostForecast;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.costexplorer.CostExplorerClient;
import software.amazon.awssdk.services.costexplorer.model.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class CostExplorerMetricsClient {

    private static final Logger log = LoggerFactory.getLogger(CostExplorerMetricsClient.class);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final CostExplorerClient costExplorerClient;
    private final IntegrationProperties integrationProperties;

    public CostExplorerMetricsClient(CostExplorerClient costExplorerClient,
                                     IntegrationProperties integrationProperties) {
        this.costExplorerClient = costExplorerClient;
        this.integrationProperties = integrationProperties;
    }

    public CostData getTotalCost(LocalDate startDate, LocalDate endDate) {
        try {
            GetCostAndUsageRequest request = GetCostAndUsageRequest.builder()
                    .timePeriod(DateInterval.builder()
                            .start(startDate.format(DATE_FORMAT))
                            .end(endDate.format(DATE_FORMAT))
                            .build())
                    .granularity(Granularity.DAILY)
                    .metrics("UnblendedCost", "UsageQuantity")
                    .build();

            GetCostAndUsageResponse response = costExplorerClient.getCostAndUsage(request);

            double totalCost = 0.0;
            String currency = "USD";
            List<CostData.DailyCost> dailyCosts = new ArrayList<>();

            for (ResultByTime result : response.resultsByTime()) {
                MetricValue costMetric = result.total().get("UnblendedCost");
                double amount = Double.parseDouble(costMetric.amount());
                totalCost += amount;
                currency = costMetric.unit();

                dailyCosts.add(CostData.DailyCost.builder()
                        .date(LocalDate.parse(result.timePeriod().start()))
                        .amount(amount)
                        .currency(currency)
                        .build());
            }

            return CostData.builder()
                    .totalAmount(totalCost)
                    .currency(currency)
                    .startDate(startDate)
                    .endDate(endDate)
                    .dailyCosts(dailyCosts)
                    .build();

        } catch (CostExplorerException e) {
            log.error("Error fetching total cost: {}", e.getMessage());
            throw new IntegrationException("CostExplorer", "Failed to fetch total cost", e);
        }
    }

    public Map<String, CostData> getCostByService(LocalDate startDate, LocalDate endDate) {
        try {
            GetCostAndUsageRequest request = GetCostAndUsageRequest.builder()
                    .timePeriod(DateInterval.builder()
                            .start(startDate.format(DATE_FORMAT))
                            .end(endDate.format(DATE_FORMAT))
                            .build())
                    .granularity(Granularity.DAILY)
                    .metrics("UnblendedCost")
                    .groupBy(GroupDefinition.builder()
                            .type(GroupDefinitionType.DIMENSION)
                            .key("SERVICE")
                            .build())
                    .build();

            GetCostAndUsageResponse response = costExplorerClient.getCostAndUsage(request);

            Map<String, List<CostData.DailyCost>> serviceDailyCosts = new HashMap<>();
            Map<String, Double> serviceTotals = new HashMap<>();

            for (ResultByTime result : response.resultsByTime()) {
                LocalDate date = LocalDate.parse(result.timePeriod().start());

                for (Group group : result.groups()) {
                    String serviceName = group.keys().get(0);
                    MetricValue costMetric = group.metrics().get("UnblendedCost");
                    double amount = Double.parseDouble(costMetric.amount());

                    serviceDailyCosts.computeIfAbsent(serviceName, k -> new ArrayList<>())
                            .add(CostData.DailyCost.builder()
                                    .date(date)
                                    .amount(amount)
                                    .currency(costMetric.unit())
                                    .build());

                    serviceTotals.merge(serviceName, amount, Double::sum);
                }
            }

            Map<String, CostData> result = new HashMap<>();
            for (Map.Entry<String, List<CostData.DailyCost>> entry : serviceDailyCosts.entrySet()) {
                result.put(entry.getKey(), CostData.builder()
                        .serviceName(entry.getKey())
                        .totalAmount(serviceTotals.get(entry.getKey()))
                        .currency("USD")
                        .startDate(startDate)
                        .endDate(endDate)
                        .dailyCosts(entry.getValue())
                        .build());
            }

            return result;

        } catch (CostExplorerException e) {
            log.error("Error fetching cost by service: {}", e.getMessage());
            throw new IntegrationException("CostExplorer", "Failed to fetch cost by service", e);
        }
    }

    public Map<String, CostData> getCostByTag(String tagKey, LocalDate startDate, LocalDate endDate) {
        try {
            GetCostAndUsageRequest request = GetCostAndUsageRequest.builder()
                    .timePeriod(DateInterval.builder()
                            .start(startDate.format(DATE_FORMAT))
                            .end(endDate.format(DATE_FORMAT))
                            .build())
                    .granularity(Granularity.MONTHLY)
                    .metrics("UnblendedCost")
                    .groupBy(GroupDefinition.builder()
                            .type(GroupDefinitionType.TAG)
                            .key(tagKey)
                            .build())
                    .build();

            GetCostAndUsageResponse response = costExplorerClient.getCostAndUsage(request);

            Map<String, Double> tagCosts = new HashMap<>();

            for (ResultByTime result : response.resultsByTime()) {
                for (Group group : result.groups()) {
                    String tagValue = group.keys().get(0);
                    if (tagValue.startsWith(tagKey + "$")) {
                        tagValue = tagValue.substring(tagKey.length() + 1);
                    }
                    MetricValue costMetric = group.metrics().get("UnblendedCost");
                    double amount = Double.parseDouble(costMetric.amount());
                    tagCosts.merge(tagValue, amount, Double::sum);
                }
            }

            Map<String, CostData> result = new HashMap<>();
            for (Map.Entry<String, Double> entry : tagCosts.entrySet()) {
                result.put(entry.getKey(), CostData.builder()
                        .tagKey(tagKey)
                        .tagValue(entry.getKey())
                        .totalAmount(entry.getValue())
                        .currency("USD")
                        .startDate(startDate)
                        .endDate(endDate)
                        .build());
            }

            return result;

        } catch (CostExplorerException e) {
            log.error("Error fetching cost by tag {}: {}", tagKey, e.getMessage());
            throw new IntegrationException("CostExplorer", "Failed to fetch cost by tag", e);
        }
    }

    public CostForecast getCostForecast(LocalDate startDate, LocalDate endDate) {
        try {
            GetCostForecastRequest request = GetCostForecastRequest.builder()
                    .timePeriod(DateInterval.builder()
                            .start(startDate.format(DATE_FORMAT))
                            .end(endDate.format(DATE_FORMAT))
                            .build())
                    .granularity(Granularity.MONTHLY)
                    .metric(com.grouplead.integration.aws.dto.Metric.UNBLENDED_COST.getValue())
                    .build();

            GetCostForecastResponse response = costExplorerClient.getCostForecast(request);

            List<CostForecast.ForecastPeriod> periods = new ArrayList<>();
            for (ForecastResult result : response.forecastResultsByTime()) {
                periods.add(CostForecast.ForecastPeriod.builder()
                        .startDate(LocalDate.parse(result.timePeriod().start()))
                        .endDate(LocalDate.parse(result.timePeriod().end()))
                        .meanValue(Double.parseDouble(result.meanValue()))
                        .build());
            }

            return CostForecast.builder()
                    .totalForecastedAmount(Double.parseDouble(response.total().amount()))
                    .currency(response.total().unit())
                    .forecastPeriods(periods)
                    .build();

        } catch (CostExplorerException e) {
            log.error("Error fetching cost forecast: {}", e.getMessage());
            throw new IntegrationException("CostExplorer", "Failed to fetch cost forecast", e);
        }
    }

    public List<CostData.ResourceCost> getTopCostResources(LocalDate startDate, LocalDate endDate, int limit) {
        try {
            GetCostAndUsageRequest request = GetCostAndUsageRequest.builder()
                    .timePeriod(DateInterval.builder()
                            .start(startDate.format(DATE_FORMAT))
                            .end(endDate.format(DATE_FORMAT))
                            .build())
                    .granularity(Granularity.MONTHLY)
                    .metrics("UnblendedCost")
                    .groupBy(
                            GroupDefinition.builder()
                                    .type(GroupDefinitionType.DIMENSION)
                                    .key("SERVICE")
                                    .build(),
                            GroupDefinition.builder()
                                    .type(GroupDefinitionType.DIMENSION)
                                    .key("USAGE_TYPE")
                                    .build()
                    )
                    .build();

            GetCostAndUsageResponse response = costExplorerClient.getCostAndUsage(request);

            Map<String, Double> resourceCosts = new HashMap<>();

            for (ResultByTime result : response.resultsByTime()) {
                for (Group group : result.groups()) {
                    String resourceKey = String.join(" - ", group.keys());
                    MetricValue costMetric = group.metrics().get("UnblendedCost");
                    double amount = Double.parseDouble(costMetric.amount());
                    resourceCosts.merge(resourceKey, amount, Double::sum);
                }
            }

            return resourceCosts.entrySet().stream()
                    .sorted((e1, e2) -> Double.compare(e2.getValue(), e1.getValue()))
                    .limit(limit)
                    .map(entry -> {
                        String[] parts = entry.getKey().split(" - ");
                        return CostData.ResourceCost.builder()
                                .service(parts.length > 0 ? parts[0] : "Unknown")
                                .usageType(parts.length > 1 ? parts[1] : "Unknown")
                                .amount(entry.getValue())
                                .currency("USD")
                                .build();
                    })
                    .toList();

        } catch (CostExplorerException e) {
            log.error("Error fetching top cost resources: {}", e.getMessage());
            throw new IntegrationException("CostExplorer", "Failed to fetch top cost resources", e);
        }
    }
}
