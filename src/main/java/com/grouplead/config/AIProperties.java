package com.grouplead.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "ai")
public class AIProperties {

    private String provider = "claude";
    private ClaudeConfig claude = new ClaudeConfig();
    private OpenAIConfig openai = new OpenAIConfig();
    private FeaturesConfig features = new FeaturesConfig();

    @Data
    public static class ClaudeConfig {
        private String apiKey;
        private String model = "claude-sonnet-4-20250514";
        private int maxTokens = 4096;
    }

    @Data
    public static class OpenAIConfig {
        private String apiKey;
        private String model = "gpt-4-turbo";
    }

    @Data
    public static class FeaturesConfig {
        private boolean predictionsEnabled = true;
        private boolean anomalyDetectionEnabled = true;
        private boolean chatEnabled = true;
        private boolean burnoutDetectionEnabled = true;
    }
}
