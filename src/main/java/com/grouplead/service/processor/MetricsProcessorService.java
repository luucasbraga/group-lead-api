package com.grouplead.service.processor;

import com.grouplead.domain.entity.Metric;
import com.grouplead.domain.enums.MetricType;
import com.grouplead.domain.enums.PeriodType;
import com.grouplead.domain.vo.DateRange;
import com.grouplead.dto.response.CodeQualityResponse;
import com.grouplead.dto.response.TimeSeriesResponse;
import com.grouplead.dto.response.VelocityResponse;
import com.grouplead.repository.CommitRepository;
import com.grouplead.repository.MetricRepository;
import com.grouplead.repository.SprintRepository;
import com.grouplead.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MetricsProcessorService {

    private final MetricRepository metricRepository;
    private final TicketRepository ticketRepository;
    private final SprintRepository sprintRepository;
    private final CommitRepository commitRepository;

    @Cacheable(value = "team-velocity", key = "#teamId + '-' + #sprintCount")
    public VelocityResponse getTeamVelocity(Long teamId, int sprintCount) {
        var sprints = sprintRepository.findLastSprintsByTeamId(teamId, sprintCount);

        List<VelocityResponse.SprintVelocity> sprintVelocities = sprints.stream()
                .map(sprint -> {
                    var tickets = ticketRepository.findBySprintId(sprint.getId());

                    int planned = tickets.stream()
                            .filter(t -> t.getStoryPoints() != null)
                            .mapToInt(t -> t.getStoryPoints())
                            .sum();

                    int completed = tickets.stream()
                            .filter(t -> t.isCompleted() && t.getStoryPoints() != null)
                            .mapToInt(t -> t.getStoryPoints())
                            .sum();

                    double completionRate = planned > 0 ? (double) completed / planned * 100 : 0;

                    return new VelocityResponse.SprintVelocity(
                            sprint.getExternalId(),
                            sprint.getName(),
                            planned,
                            completed,
                            completionRate
                    );
                })
                .sorted(Comparator.comparing(VelocityResponse.SprintVelocity::sprintId))
                .toList();

        double averageVelocity = sprintVelocities.stream()
                .mapToInt(VelocityResponse.SprintVelocity::completedStoryPoints)
                .average()
                .orElse(0);

        // Calculate trend (comparing last 3 sprints vs previous 3)
        double velocityTrend = calculateVelocityTrend(sprintVelocities);

        return new VelocityResponse(
                teamId,
                sprintCount,
                averageVelocity,
                velocityTrend,
                sprintVelocities
        );
    }

    public TimeSeriesResponse getTimeSeries(MetricType metricType, LocalDate startDate,
                                            LocalDate endDate, String granularity) {
        DateRange range = DateRange.of(startDate, endDate);

        var metrics = metricRepository.findByTypeAndPeriod(metricType, range.start(), range.end());

        List<TimeSeriesResponse.DataPoint> dataPoints = metrics.stream()
                .map(m -> new TimeSeriesResponse.DataPoint(m.getTimestamp(), m.getValue()))
                .toList();

        TimeSeriesResponse.Statistics stats = calculateStatistics(metrics);

        return new TimeSeriesResponse(metricType, granularity, dataPoints, stats);
    }

    public CodeQualityResponse getCodeQualityMetrics(DateRange range) {
        // Placeholder implementation
        return new CodeQualityResponse(
                75.0, 2.5,
                1000, 990, 10,
                5, -20,
                40.0,
                List.of(),
                List.of()
        );
    }

    public void saveMetric(MetricType type, String name, Double value, String unit, String source) {
        Metric metric = Metric.builder()
                .type(type)
                .name(name)
                .value(value)
                .unit(unit)
                .source(source)
                .timestamp(LocalDateTime.now())
                .build();

        metricRepository.save(metric);
    }

    private double calculateVelocityTrend(List<VelocityResponse.SprintVelocity> sprints) {
        if (sprints.size() < 6) return 0;

        double recent = sprints.subList(sprints.size() - 3, sprints.size()).stream()
                .mapToInt(VelocityResponse.SprintVelocity::completedStoryPoints)
                .average()
                .orElse(0);

        double previous = sprints.subList(sprints.size() - 6, sprints.size() - 3).stream()
                .mapToInt(VelocityResponse.SprintVelocity::completedStoryPoints)
                .average()
                .orElse(0);

        if (previous == 0) return 0;
        return ((recent - previous) / previous) * 100;
    }

    private TimeSeriesResponse.Statistics calculateStatistics(List<Metric> metrics) {
        if (metrics.isEmpty()) {
            return new TimeSeriesResponse.Statistics(0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
        }

        List<Double> values = metrics.stream()
                .map(Metric::getValue)
                .sorted()
                .toList();

        double min = values.getFirst();
        double max = values.getLast();
        double avg = values.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double median = values.get(values.size() / 2);
        double p95 = values.get((int) (values.size() * 0.95));
        double p99 = values.get((int) (values.size() * 0.99));

        return new TimeSeriesResponse.Statistics(min, max, avg, median, p95, p99);
    }
}
