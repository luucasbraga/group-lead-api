package com.grouplead.service.collector;

import com.grouplead.domain.entity.Metric;
import com.grouplead.domain.entity.Team;
import com.grouplead.domain.enums.MetricType;
import com.grouplead.integration.aws.CloudWatchMetricsClient;
import com.grouplead.integration.aws.CostExplorerMetricsClient;
import com.grouplead.integration.aws.dto.CloudWatchMetric;
import com.grouplead.integration.aws.dto.CostData;
import com.grouplead.integration.aws.dto.CostForecast;
import com.grouplead.repository.MetricRepository;
import com.grouplead.repository.TeamRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class CloudWatchCollectorService {

    private static final Logger log = LoggerFactory.getLogger(CloudWatchCollectorService.class);

    private final CloudWatchMetricsClient cloudWatchClient;
    private final CostExplorerMetricsClient costExplorerClient;
    private final MetricRepository metricRepository;
    private final TeamRepository teamRepository;

    public CloudWatchCollectorService(CloudWatchMetricsClient cloudWatchClient,
                                      CostExplorerMetricsClient costExplorerClient,
                                      MetricRepository metricRepository,
                                      TeamRepository teamRepository) {
        this.cloudWatchClient = cloudWatchClient;
        this.costExplorerClient = costExplorerClient;
        this.metricRepository = metricRepository;
        this.teamRepository = teamRepository;
    }

    @Transactional
    public CollectionResult collectEC2Metrics(String instanceId, Long teamId, LocalDateTime since) {
        log.info("Collecting EC2 metrics for instance {} since {}", instanceId, since);

        Team team = teamRepository.findById(teamId).orElse(null);
        LocalDateTime now = LocalDateTime.now();

        List<CloudWatchMetric> cloudWatchMetrics = cloudWatchClient.getEC2Metrics(instanceId, since, now);
        List<Metric> savedMetrics = new ArrayList<>();

        for (CloudWatchMetric cwMetric : cloudWatchMetrics) {
            Metric metric = createMetric(cwMetric, team, "ec2:" + instanceId);
            savedMetrics.add(metricRepository.save(metric));
        }

        log.info("Collected {} EC2 metrics for instance {}", savedMetrics.size(), instanceId);
        return new CollectionResult(savedMetrics.size(), 0);
    }

    @Transactional
    public CollectionResult collectRDSMetrics(String dbInstanceId, Long teamId, LocalDateTime since) {
        log.info("Collecting RDS metrics for instance {} since {}", dbInstanceId, since);

        Team team = teamRepository.findById(teamId).orElse(null);
        LocalDateTime now = LocalDateTime.now();

        List<CloudWatchMetric> cloudWatchMetrics = cloudWatchClient.getRDSMetrics(dbInstanceId, since, now);
        List<Metric> savedMetrics = new ArrayList<>();

        for (CloudWatchMetric cwMetric : cloudWatchMetrics) {
            Metric metric = createMetric(cwMetric, team, "rds:" + dbInstanceId);
            savedMetrics.add(metricRepository.save(metric));
        }

        log.info("Collected {} RDS metrics for instance {}", savedMetrics.size(), dbInstanceId);
        return new CollectionResult(savedMetrics.size(), 0);
    }

    @Transactional
    public CollectionResult collectECSMetrics(String clusterName, String serviceName, Long teamId, LocalDateTime since) {
        log.info("Collecting ECS metrics for service {}/{} since {}", clusterName, serviceName, since);

        Team team = teamRepository.findById(teamId).orElse(null);
        LocalDateTime now = LocalDateTime.now();

        List<CloudWatchMetric> cloudWatchMetrics = cloudWatchClient.getECSMetrics(clusterName, serviceName, since, now);
        List<Metric> savedMetrics = new ArrayList<>();

        for (CloudWatchMetric cwMetric : cloudWatchMetrics) {
            Metric metric = createMetric(cwMetric, team, "ecs:" + clusterName + "/" + serviceName);
            savedMetrics.add(metricRepository.save(metric));
        }

        log.info("Collected {} ECS metrics for service {}/{}", savedMetrics.size(), clusterName, serviceName);
        return new CollectionResult(savedMetrics.size(), 0);
    }

    @Transactional
    public CollectionResult collectLambdaMetrics(String functionName, Long teamId, LocalDateTime since) {
        log.info("Collecting Lambda metrics for function {} since {}", functionName, since);

        Team team = teamRepository.findById(teamId).orElse(null);
        LocalDateTime now = LocalDateTime.now();

        List<CloudWatchMetric> cloudWatchMetrics = cloudWatchClient.getLambdaMetrics(functionName, since, now);
        List<Metric> savedMetrics = new ArrayList<>();

        for (CloudWatchMetric cwMetric : cloudWatchMetrics) {
            Metric metric = createMetric(cwMetric, team, "lambda:" + functionName);
            savedMetrics.add(metricRepository.save(metric));
        }

        log.info("Collected {} Lambda metrics for function {}", savedMetrics.size(), functionName);
        return new CollectionResult(savedMetrics.size(), 0);
    }

    @Transactional
    public CollectionResult collectCostMetrics(Long teamId, LocalDate startDate, LocalDate endDate) {
        log.info("Collecting cost metrics from {} to {}", startDate, endDate);

        Team team = teamRepository.findById(teamId).orElse(null);
        List<Metric> savedMetrics = new ArrayList<>();

        // Total cost
        CostData totalCost = costExplorerClient.getTotalCost(startDate, endDate);
        Metric totalCostMetric = Metric.builder()
                .team(team)
                .type(MetricType.COST)
                .name("total_cost")
                .value(totalCost.getTotalAmount())
                .unit(totalCost.getCurrency())
                .timestamp(LocalDateTime.now())
                .metadata(Map.of(
                        "start_date", startDate.toString(),
                        "end_date", endDate.toString()
                ))
                .build();
        savedMetrics.add(metricRepository.save(totalCostMetric));

        // Cost by service
        Map<String, CostData> costByService = costExplorerClient.getCostByService(startDate, endDate);
        for (Map.Entry<String, CostData> entry : costByService.entrySet()) {
            Metric serviceCostMetric = Metric.builder()
                    .team(team)
                    .type(MetricType.COST)
                    .name("service_cost")
                    .value(entry.getValue().getTotalAmount())
                    .unit(entry.getValue().getCurrency())
                    .timestamp(LocalDateTime.now())
                    .metadata(Map.of(
                            "service", entry.getKey(),
                            "start_date", startDate.toString(),
                            "end_date", endDate.toString()
                    ))
                    .build();
            savedMetrics.add(metricRepository.save(serviceCostMetric));
        }

        log.info("Collected {} cost metrics", savedMetrics.size());
        return new CollectionResult(savedMetrics.size(), 0);
    }

    @Transactional
    public CostForecast getCostForecast(LocalDate startDate, LocalDate endDate) {
        log.info("Getting cost forecast from {} to {}", startDate, endDate);
        return costExplorerClient.getCostForecast(startDate, endDate);
    }

    @Transactional(readOnly = true)
    public List<CostData.ResourceCost> getTopCostResources(LocalDate startDate, LocalDate endDate, int limit) {
        log.info("Getting top {} cost resources from {} to {}", limit, startDate, endDate);
        return costExplorerClient.getTopCostResources(startDate, endDate, limit);
    }

    private Metric createMetric(CloudWatchMetric cwMetric, Team team, String resourceId) {
        MetricType metricType = mapToMetricType(cwMetric.getNamespace(), cwMetric.getMetricName());

        return Metric.builder()
                .team(team)
                .type(metricType)
                .name(cwMetric.getMetricName().toLowerCase())
                .value(cwMetric.getAverageValue())
                .unit(cwMetric.getUnit())
                .timestamp(LocalDateTime.now())
                .metadata(Map.of(
                        "namespace", cwMetric.getNamespace(),
                        "resource_id", resourceId,
                        "dimension_name", cwMetric.getDimensionName() != null ? cwMetric.getDimensionName() : "",
                        "dimension_value", cwMetric.getDimensionValue() != null ? cwMetric.getDimensionValue() : "",
                        "max_value", String.valueOf(cwMetric.getMaximumValue()),
                        "min_value", String.valueOf(cwMetric.getMinimumValue()),
                        "data_point_count", String.valueOf(cwMetric.getDataPointCount())
                ))
                .build();
    }

    private MetricType mapToMetricType(String namespace, String metricName) {
        if (metricName.toLowerCase().contains("cpu")) {
            return MetricType.CPU_UTILIZATION;
        } else if (metricName.toLowerCase().contains("memory")) {
            return MetricType.MEMORY_UTILIZATION;
        } else if (metricName.toLowerCase().contains("network")) {
            return MetricType.NETWORK_THROUGHPUT;
        } else if (metricName.toLowerCase().contains("error")) {
            return MetricType.ERROR_RATE;
        } else if (metricName.toLowerCase().contains("latency") || metricName.toLowerCase().contains("duration")) {
            return MetricType.LATENCY;
        } else if (metricName.toLowerCase().contains("request") || metricName.toLowerCase().contains("invocation")) {
            return MetricType.REQUEST_COUNT;
        }

        return switch (namespace) {
            case "AWS/EC2", "AWS/ECS", "AWS/Lambda" -> MetricType.CPU_UTILIZATION;
            case "AWS/RDS" -> MetricType.DATABASE_CONNECTIONS;
            case "AWS/ApplicationELB" -> MetricType.REQUEST_COUNT;
            default -> MetricType.CUSTOM;
        };
    }

    public record CollectionResult(int collected, int errors) {}
}
