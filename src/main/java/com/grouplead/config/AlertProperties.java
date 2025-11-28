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

    @Data
    public static class ThresholdsConfig {
        private int latencyP99Ms = 500;
        private double errorRatePercent = 1.0;
        private int cpuPercent = 80;
        private int memoryPercent = 85;
        private double deploymentFailureRate = 0.1;
    }

    @Data
    public static class NotificationsConfig {
        private String slackWebhook;
        private boolean emailEnabled = false;
        private String emailRecipients;
    }
}
