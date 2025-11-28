package com.grouplead.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "integrations")
public class IntegrationProperties {

    private JiraConfig jira = new JiraConfig();
    private ZohoConfig zoho = new ZohoConfig();
    private GitLabConfig gitlab = new GitLabConfig();
    private AwsConfig aws = new AwsConfig();

    @Data
    public static class JiraConfig {
        private String baseUrl;
        private String apiToken;
        private String email;
        private String projectKeys;
        private String boardId;
    }

    @Data
    public static class ZohoConfig {
        private String baseUrl;
        private String clientId;
        private String clientSecret;
        private String refreshToken;
    }

    @Data
    public static class GitLabConfig {
        private String baseUrl;
        private String privateToken;
        private String projectIds;
    }

    @Data
    public static class AwsConfig {
        private String region;
        private String accessKey;
        private String secretKey;
        private CloudWatchConfig cloudwatch = new CloudWatchConfig();

        @Data
        public static class CloudWatchConfig {
            private String namespaces;
        }
    }
}
