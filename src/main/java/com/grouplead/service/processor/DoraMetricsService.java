package com.grouplead.service.processor;

import com.grouplead.domain.enums.DoraClassification;
import com.grouplead.domain.vo.DateRange;
import com.grouplead.domain.vo.DoraMetrics;
import com.grouplead.repository.DeploymentRepository;
import com.grouplead.repository.IncidentRepository;
import com.grouplead.repository.MergeRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class DoraMetricsService {

    private final DeploymentRepository deploymentRepository;
    private final MergeRequestRepository mergeRequestRepository;
    private final IncidentRepository incidentRepository;

    @Cacheable(value = "dora-metrics", key = "#range.start().toString() + '-' + #range.end().toString()")
    public DoraMetrics calculateMetrics(DateRange range) {
        return new DoraMetrics(
                calculateDeploymentFrequency(range),
                calculateLeadTime(range),
                calculateChangeFailureRate(range),
                calculateMTTR(range)
        );
    }

    public DoraMetrics.DeploymentFrequency calculateDeploymentFrequency(DateRange range) {
        var deployments = deploymentRepository.findByPeriod(range.start(), range.end());
        long totalDays = ChronoUnit.DAYS.between(range.start(), range.end());

        if (totalDays == 0) totalDays = 1;

        double deploysPerDay = (double) deployments.size() / totalDays;
        double deploysPerWeek = deploysPerDay * 7;

        DoraClassification classification = classifyDeploymentFrequency(deploysPerDay);

        // Calculate trend - simplified
        String trend = "stable";
        if (deploysPerDay > 1) trend = "up";
        else if (deploysPerDay < 0.14) trend = "down"; // less than weekly

        return new DoraMetrics.DeploymentFrequency(
                deployments.size(),
                deploysPerDay,
                deploysPerWeek,
                classification,
                trend
        );
    }

    public DoraMetrics.LeadTimeForChanges calculateLeadTime(DateRange range) {
        var mergeRequests = mergeRequestRepository.findMergedInPeriod(range.start(), range.end());

        if (mergeRequests.isEmpty()) {
            return DoraMetrics.LeadTimeForChanges.empty();
        }

        var leadTimes = mergeRequests.stream()
                .filter(mr -> mr.getDeployedAt() != null && mr.getCreatedAt() != null)
                .map(mr -> (double) ChronoUnit.HOURS.between(mr.getCreatedAt(), mr.getDeployedAt()))
                .sorted()
                .toList();

        if (leadTimes.isEmpty()) {
            return DoraMetrics.LeadTimeForChanges.empty();
        }

        double avg = leadTimes.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double median = leadTimes.get(leadTimes.size() / 2);
        double p90 = leadTimes.get((int) (leadTimes.size() * 0.9));

        return new DoraMetrics.LeadTimeForChanges(
                avg,
                median,
                p90,
                classifyLeadTime(avg)
        );
    }

    public DoraMetrics.ChangeFailureRate calculateChangeFailureRate(DateRange range) {
        var deployments = deploymentRepository.findByPeriod(range.start(), range.end());
        long failedDeployments = deploymentRepository.countFailedDeploymentsInPeriod(range.start(), range.end());

        if (deployments.isEmpty()) {
            return DoraMetrics.ChangeFailureRate.empty();
        }

        double rate = (double) failedDeployments / deployments.size() * 100;

        return new DoraMetrics.ChangeFailureRate(
                deployments.size(),
                (int) failedDeployments,
                rate,
                classifyChangeFailureRate(rate)
        );
    }

    public DoraMetrics.MeanTimeToRecovery calculateMTTR(DateRange range) {
        var incidents = incidentRepository.findResolvedInPeriod(range.start(), range.end());

        if (incidents.isEmpty()) {
            return DoraMetrics.MeanTimeToRecovery.empty();
        }

        double avgMinutes = incidents.stream()
                .filter(i -> i.getRecoveryTimeMinutes() != null)
                .mapToLong(i -> i.getRecoveryTimeMinutes())
                .average()
                .orElse(0);

        return new DoraMetrics.MeanTimeToRecovery(
                avgMinutes,
                incidents.size(),
                classifyMTTR(avgMinutes)
        );
    }

    private DoraClassification classifyDeploymentFrequency(double deploysPerDay) {
        if (deploysPerDay >= 1) return DoraClassification.ELITE;
        if (deploysPerDay >= 1.0 / 7) return DoraClassification.HIGH;
        if (deploysPerDay >= 1.0 / 30) return DoraClassification.MEDIUM;
        return DoraClassification.LOW;
    }

    private DoraClassification classifyLeadTime(double avgHours) {
        if (avgHours < 24) return DoraClassification.ELITE;
        if (avgHours < 168) return DoraClassification.HIGH; // 1 week
        if (avgHours < 720) return DoraClassification.MEDIUM; // 1 month
        return DoraClassification.LOW;
    }

    private DoraClassification classifyChangeFailureRate(double rate) {
        if (rate <= 5) return DoraClassification.ELITE;
        if (rate <= 10) return DoraClassification.HIGH;
        if (rate <= 15) return DoraClassification.MEDIUM;
        return DoraClassification.LOW;
    }

    private DoraClassification classifyMTTR(double avgMinutes) {
        if (avgMinutes < 60) return DoraClassification.ELITE;
        if (avgMinutes < 1440) return DoraClassification.HIGH; // 1 day
        if (avgMinutes < 10080) return DoraClassification.MEDIUM; // 1 week
        return DoraClassification.LOW;
    }
}
