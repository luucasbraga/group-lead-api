package com.grouplead.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.costexplorer.CostExplorerClient;

@Configuration
public class AwsConfig {

    @Value("${aws.access-key-id:}")
    private String accessKeyId;

    @Value("${aws.secret-access-key:}")
    private String secretAccessKey;

    @Value("${aws.region:us-east-1}")
    private String region;

    @Bean
    public CloudWatchClient cloudWatchClient() {
        if (accessKeyId.isEmpty() || secretAccessKey.isEmpty()) {
            // Use default credential provider chain (IAM roles, environment variables, etc.)
            return CloudWatchClient.builder()
                    .region(Region.of(region))
                    .build();
        }

        return CloudWatchClient.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKeyId, secretAccessKey)))
                .build();
    }

    @Bean
    public CostExplorerClient costExplorerClient() {
        if (accessKeyId.isEmpty() || secretAccessKey.isEmpty()) {
            // Use default credential provider chain
            return CostExplorerClient.builder()
                    .region(Region.US_EAST_1) // Cost Explorer is only available in us-east-1
                    .build();
        }

        return CostExplorerClient.builder()
                .region(Region.US_EAST_1) // Cost Explorer is only available in us-east-1
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKeyId, secretAccessKey)))
                .build();
    }
}
