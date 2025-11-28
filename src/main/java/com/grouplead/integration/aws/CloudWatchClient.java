package com.grouplead.integration.aws;

import com.grouplead.config.IntegrationProperties;
import com.grouplead.exception.IntegrationException;
import com.grouplead.integration.aws.dto.CloudWatchMetric;
import com.grouplead.integration.aws.dto.CloudWatchMetricDataResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.cloudwatch.model.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Component
public class CloudWatchMetricsClient {

    private static final Logger log = LoggerFactory.getLogger(CloudWatchMetricsClient.class);

    private final CloudWatchClient cloudWatchClient;
    private final IntegrationProperties integrationProperties;

    public CloudWatchMetricsClient(CloudWatchClient cloudWatchClient,
                                   IntegrationProperties integrationProperties) {
        this.cloudWatchClient = cloudWatchClient;
        this.integrationProperties = integrationProperties;
    }

    public List<CloudWatchMetric> getEC2Metrics(String instanceId, LocalDateTime start, LocalDateTime end) {
        List<CloudWatchMetric> metrics = new ArrayList<>();

        try {
            metrics.add(getMetric("AWS/EC2", "CPUUtilization", instanceId, "InstanceId", start, end));
            metrics.add(getMetric("AWS/EC2", "NetworkIn", instanceId, "InstanceId", start, end));
            metrics.add(getMetric("AWS/EC2", "NetworkOut", instanceId, "InstanceId", start, end));
            metrics.add(getMetric("AWS/EC2", "DiskReadBytes", instanceId, "InstanceId", start, end));
            metrics.add(getMetric("AWS/EC2", "DiskWriteBytes", instanceId, "InstanceId", start, end));
        } catch (CloudWatchException e) {
            log.error("Error fetching EC2 metrics for instance {}: {}", instanceId, e.getMessage());
            throw new IntegrationException("CloudWatch", "Failed to fetch EC2 metrics", e);
        }

        return metrics;
    }

    public List<CloudWatchMetric> getRDSMetrics(String dbInstanceId, LocalDateTime start, LocalDateTime end) {
        List<CloudWatchMetric> metrics = new ArrayList<>();

        try {
            metrics.add(getMetric("AWS/RDS", "CPUUtilization", dbInstanceId, "DBInstanceIdentifier", start, end));
            metrics.add(getMetric("AWS/RDS", "DatabaseConnections", dbInstanceId, "DBInstanceIdentifier", start, end));
            metrics.add(getMetric("AWS/RDS", "FreeableMemory", dbInstanceId, "DBInstanceIdentifier", start, end));
            metrics.add(getMetric("AWS/RDS", "ReadIOPS", dbInstanceId, "DBInstanceIdentifier", start, end));
            metrics.add(getMetric("AWS/RDS", "WriteIOPS", dbInstanceId, "DBInstanceIdentifier", start, end));
            metrics.add(getMetric("AWS/RDS", "FreeStorageSpace", dbInstanceId, "DBInstanceIdentifier", start, end));
        } catch (CloudWatchException e) {
            log.error("Error fetching RDS metrics for instance {}: {}", dbInstanceId, e.getMessage());
            throw new IntegrationException("CloudWatch", "Failed to fetch RDS metrics", e);
        }

        return metrics;
    }

    public List<CloudWatchMetric> getECSMetrics(String clusterName, String serviceName,
                                                 LocalDateTime start, LocalDateTime end) {
        List<CloudWatchMetric> metrics = new ArrayList<>();

        try {
            GetMetricDataRequest request = GetMetricDataRequest.builder()
                    .startTime(toInstant(start))
                    .endTime(toInstant(end))
                    .metricDataQueries(
                            buildMetricQuery("cpu", "AWS/ECS", "CPUUtilization",
                                    List.of(
                                            Dimension.builder().name("ClusterName").value(clusterName).build(),
                                            Dimension.builder().name("ServiceName").value(serviceName).build()
                                    )),
                            buildMetricQuery("memory", "AWS/ECS", "MemoryUtilization",
                                    List.of(
                                            Dimension.builder().name("ClusterName").value(clusterName).build(),
                                            Dimension.builder().name("ServiceName").value(serviceName).build()
                                    ))
                    )
                    .build();

            GetMetricDataResponse response = cloudWatchClient.getMetricData(request);

            for (MetricDataResult result : response.metricDataResults()) {
                metrics.add(mapToCloudWatchMetric(result, "AWS/ECS", serviceName));
            }
        } catch (CloudWatchException e) {
            log.error("Error fetching ECS metrics for service {}: {}", serviceName, e.getMessage());
            throw new IntegrationException("CloudWatch", "Failed to fetch ECS metrics", e);
        }

        return metrics;
    }

    public List<CloudWatchMetric> getLambdaMetrics(String functionName, LocalDateTime start, LocalDateTime end) {
        List<CloudWatchMetric> metrics = new ArrayList<>();

        try {
            metrics.add(getMetric("AWS/Lambda", "Invocations", functionName, "FunctionName", start, end));
            metrics.add(getMetric("AWS/Lambda", "Duration", functionName, "FunctionName", start, end));
            metrics.add(getMetric("AWS/Lambda", "Errors", functionName, "FunctionName", start, end));
            metrics.add(getMetric("AWS/Lambda", "Throttles", functionName, "FunctionName", start, end));
            metrics.add(getMetric("AWS/Lambda", "ConcurrentExecutions", functionName, "FunctionName", start, end));
        } catch (CloudWatchException e) {
            log.error("Error fetching Lambda metrics for function {}: {}", functionName, e.getMessage());
            throw new IntegrationException("CloudWatch", "Failed to fetch Lambda metrics", e);
        }

        return metrics;
    }

    public List<CloudWatchMetric> getALBMetrics(String loadBalancerArn, LocalDateTime start, LocalDateTime end) {
        List<CloudWatchMetric> metrics = new ArrayList<>();
        String loadBalancer = extractLoadBalancerName(loadBalancerArn);

        try {
            metrics.add(getMetric("AWS/ApplicationELB", "RequestCount", loadBalancer, "LoadBalancer", start, end));
            metrics.add(getMetric("AWS/ApplicationELB", "TargetResponseTime", loadBalancer, "LoadBalancer", start, end));
            metrics.add(getMetric("AWS/ApplicationELB", "HTTPCode_Target_2XX_Count", loadBalancer, "LoadBalancer", start, end));
            metrics.add(getMetric("AWS/ApplicationELB", "HTTPCode_Target_4XX_Count", loadBalancer, "LoadBalancer", start, end));
            metrics.add(getMetric("AWS/ApplicationELB", "HTTPCode_Target_5XX_Count", loadBalancer, "LoadBalancer", start, end));
        } catch (CloudWatchException e) {
            log.error("Error fetching ALB metrics for {}: {}", loadBalancer, e.getMessage());
            throw new IntegrationException("CloudWatch", "Failed to fetch ALB metrics", e);
        }

        return metrics;
    }

    public CloudWatchMetricDataResult getMetricStatistics(String namespace, String metricName,
                                                           String dimensionName, String dimensionValue,
                                                           LocalDateTime start, LocalDateTime end,
                                                           int periodSeconds, Statistic statistic) {
        try {
            GetMetricStatisticsRequest request = GetMetricStatisticsRequest.builder()
                    .namespace(namespace)
                    .metricName(metricName)
                    .dimensions(Dimension.builder()
                            .name(dimensionName)
                            .value(dimensionValue)
                            .build())
                    .startTime(toInstant(start))
                    .endTime(toInstant(end))
                    .period(periodSeconds)
                    .statistics(statistic)
                    .build();

            GetMetricStatisticsResponse response = cloudWatchClient.getMetricStatistics(request);

            List<CloudWatchMetricDataResult.DataPoint> dataPoints = response.datapoints().stream()
                    .map(dp -> CloudWatchMetricDataResult.DataPoint.builder()
                            .timestamp(LocalDateTime.ofInstant(dp.timestamp(), ZoneOffset.UTC))
                            .value(getStatisticValue(dp, statistic))
                            .unit(dp.unitAsString())
                            .build())
                    .toList();

            return CloudWatchMetricDataResult.builder()
                    .namespace(namespace)
                    .metricName(metricName)
                    .dimensionName(dimensionName)
                    .dimensionValue(dimensionValue)
                    .dataPoints(dataPoints)
                    .build();

        } catch (CloudWatchException e) {
            log.error("Error fetching metric statistics for {}/{}: {}", namespace, metricName, e.getMessage());
            throw new IntegrationException("CloudWatch", "Failed to fetch metric statistics", e);
        }
    }

    private CloudWatchMetric getMetric(String namespace, String metricName,
                                        String dimensionValue, String dimensionName,
                                        LocalDateTime start, LocalDateTime end) {
        GetMetricStatisticsRequest request = GetMetricStatisticsRequest.builder()
                .namespace(namespace)
                .metricName(metricName)
                .dimensions(Dimension.builder()
                        .name(dimensionName)
                        .value(dimensionValue)
                        .build())
                .startTime(toInstant(start))
                .endTime(toInstant(end))
                .period(300) // 5 minutes
                .statistics(Statistic.AVERAGE, Statistic.MAXIMUM, Statistic.MINIMUM)
                .build();

        GetMetricStatisticsResponse response = cloudWatchClient.getMetricStatistics(request);

        double avgValue = response.datapoints().stream()
                .mapToDouble(Datapoint::average)
                .average()
                .orElse(0.0);

        double maxValue = response.datapoints().stream()
                .mapToDouble(Datapoint::maximum)
                .max()
                .orElse(0.0);

        double minValue = response.datapoints().stream()
                .mapToDouble(Datapoint::minimum)
                .min()
                .orElse(0.0);

        String unit = response.datapoints().isEmpty() ? "None" :
                response.datapoints().get(0).unitAsString();

        return CloudWatchMetric.builder()
                .namespace(namespace)
                .metricName(metricName)
                .dimensionName(dimensionName)
                .dimensionValue(dimensionValue)
                .averageValue(avgValue)
                .maximumValue(maxValue)
                .minimumValue(minValue)
                .unit(unit)
                .startTime(start)
                .endTime(end)
                .dataPointCount(response.datapoints().size())
                .build();
    }

    private MetricDataQuery buildMetricQuery(String id, String namespace, String metricName,
                                              List<Dimension> dimensions) {
        return MetricDataQuery.builder()
                .id(id)
                .metricStat(MetricStat.builder()
                        .metric(Metric.builder()
                                .namespace(namespace)
                                .metricName(metricName)
                                .dimensions(dimensions)
                                .build())
                        .period(300)
                        .stat("Average")
                        .build())
                .returnData(true)
                .build();
    }

    private CloudWatchMetric mapToCloudWatchMetric(MetricDataResult result, String namespace, String resourceId) {
        double avgValue = result.values().stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);

        double maxValue = result.values().stream()
                .mapToDouble(Double::doubleValue)
                .max()
                .orElse(0.0);

        double minValue = result.values().stream()
                .mapToDouble(Double::doubleValue)
                .min()
                .orElse(0.0);

        return CloudWatchMetric.builder()
                .namespace(namespace)
                .metricName(result.id())
                .dimensionValue(resourceId)
                .averageValue(avgValue)
                .maximumValue(maxValue)
                .minimumValue(minValue)
                .dataPointCount(result.values().size())
                .build();
    }

    private double getStatisticValue(Datapoint datapoint, Statistic statistic) {
        return switch (statistic) {
            case AVERAGE -> datapoint.average();
            case MAXIMUM -> datapoint.maximum();
            case MINIMUM -> datapoint.minimum();
            case SUM -> datapoint.sum();
            case SAMPLE_COUNT -> datapoint.sampleCount();
            default -> datapoint.average();
        };
    }

    private Instant toInstant(LocalDateTime dateTime) {
        return dateTime.toInstant(ZoneOffset.UTC);
    }

    private String extractLoadBalancerName(String loadBalancerArn) {
        // ARN format: arn:aws:elasticloadbalancing:region:account-id:loadbalancer/app/name/id
        String[] parts = loadBalancerArn.split("/");
        if (parts.length >= 3) {
            return "app/" + parts[parts.length - 2] + "/" + parts[parts.length - 1];
        }
        return loadBalancerArn;
    }
}
