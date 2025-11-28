package com.grouplead.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "alerts")
public class AlertProperties {

    private ThresholdsConfig thresholds = new ThresholdsConfig();
    private NotificationsConfig notifications = new NotificationsConfig();

    // Convenience getters for direct access to common thresholds
    public double getVelocityDropThreshold() {
        return thresholds.getVelocityDropPercent();
    }

    public double getAfterHoursThreshold() {
        return thresholds.getAfterHoursPercent();
    }

    public int getWeekendWorkThreshold() {
        return thresholds.getWeekendCommits();
    }

    public double getCpuThreshold() {
        return thresholds.getCpuPercent();
    }

    public double getMemoryThreshold() {
        return thresholds.getMemoryPercent();
    }

    public double getErrorRateThreshold() {
        return thresholds.getErrorRatePercent();
    }

    @Data
    public static class ThresholdsConfig {
        // Performance thresholds
        private int latencyP99Ms = 500;
        private double errorRatePercent = 1.0;
        private int cpuPercent = 80;
        private int memoryPercent = 85;
        private double deploymentFailureRate = 0.1;

        // Velocity and burnout thresholds
        private double velocityDropPercent = 20.0;
        private double afterHoursPercent = 30.0;
        private int weekendCommits = 5;

        // Infrastructure thresholds
        private double diskUsagePercent = 85.0;
        private int responseTimeMs = 1000;
        private double availabilityPercent = 99.9;
    }

    @Data
    public static class NotificationsConfig {
        private String slackWebhook;
        private boolean emailEnabled = false;
        private String emailRecipients;
        private boolean pushEnabled = false;
    }
}
