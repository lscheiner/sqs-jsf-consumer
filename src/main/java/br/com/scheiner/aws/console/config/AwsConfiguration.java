package br.com.scheiner.aws.console.config;

import java.util.concurrent.atomic.AtomicReference;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import software.amazon.awssdk.regions.Region;

@Component
public class AwsConfiguration {

    private final AtomicReference<String> endpointRef = new AtomicReference<>();

    private final AtomicReference<Region> regionRef = new AtomicReference<>();

    public AwsConfiguration(
            @Value("${aws.endpoint}") String endpoint,
            @Value("${aws.region}") String region
    ) {

        this.endpointRef.set(endpoint);
        this.regionRef.set(Region.of(region));
    }

    public String getEndpoint() {
        return this.endpointRef.get();
    }

    public void setEndpoint(String endpoint) {
        this.endpointRef.set(endpoint);
    }

    public Region getRegion() {
        return this.regionRef.get();
    }

    public void setRegion(Region region) {
        this.regionRef.set(region);
    }

}
