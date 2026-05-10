package br.com.scheiner.sqs.console.config;

import java.net.URI;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;

@Component
public class SqsClientProvider {

    public static final String ACCOUNT_ID = "000000000000";

    private final AtomicReference<Region> regionRef = new AtomicReference<>();

    private final AtomicReference<String> endpointRef = new AtomicReference<>();

    private final AtomicReference<SqsClient> clientRef = new AtomicReference<>();

    public SqsClientProvider(
            @Value("${aws.region}") String region,
            @Value("${aws.sqs.endpoint}") String endpoint) {

        reconfigurar(endpoint, Region.of(region));
    }

    public void reconfigurar(String endpoint, Region region) {

        validar(endpoint, region);

        var novoClient = criarClient(endpoint, region);

        var antigo = clientRef.getAndSet(novoClient);

        endpointRef.set(endpoint);
        regionRef.set(region);

        if (antigo != null) {
            antigo.close();
        }
    }

    private SqsClient criarClient(String endpoint, Region region) {

        return SqsClient.builder()
                .endpointOverride(URI.create(endpoint))
                .region(region)
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create("test", "test")
                        )
                )
                .build();
    }

    private void validar(String endpoint, Region region) {

        if (endpoint == null || endpoint.isBlank()) {
            throw new IllegalArgumentException("Endpoint inválido");
        }

        if (region == null) {
            throw new IllegalArgumentException("Region inválida");
        }
    }

    public String montarQueueUrl(String fila) {

        return "%s/%s/%s".formatted(
                endpointRef.get(),
                ACCOUNT_ID,
                fila);
    }

    public SqsClient getClient() {
        return clientRef.get();
    }

    public Region getRegion() {
        return regionRef.get();
    }

    public String getEndpoint() {
        return endpointRef.get();
    }
}