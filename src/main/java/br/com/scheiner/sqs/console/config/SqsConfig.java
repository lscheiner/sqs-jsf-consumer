package br.com.scheiner.sqs.console.config;

import java.net.URI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;

@Configuration
public class SqsConfig {

    private final String region;

    private final String endpoint;

    public SqsConfig(
            @Value("${aws.region}") String region,
            @Value("${aws.sqs.endpoint}") String endpoint) {

        this.region = region;
        this.endpoint = endpoint;
    }

    @Bean
    public SqsClient sqsClient() {

        return SqsClient.builder()
                .region(Region.of(region))
                .endpointOverride(URI.create(endpoint))
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(
                                        "test",
                                        "test")))
                .build();
    }
}