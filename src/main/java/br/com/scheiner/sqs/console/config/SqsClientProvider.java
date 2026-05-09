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

    private volatile String region;

    private volatile String endpoint;

    private final AtomicReference<SqsClient> clientRef =   new AtomicReference<>();

    public SqsClientProvider(
            @Value("${aws.region}") String region,
            @Value("${aws.sqs.endpoint}") String endpoint) {

        reconfigurar(endpoint, region);
    }

    public void reconfigurar(String endpoint, String region) {

        validar(endpoint, region);

        var novoClient = criarClient(endpoint, region);

        var antigo = clientRef.getAndSet(novoClient);

        this.endpoint = endpoint;
        this.region = region;

        if (antigo != null) {
            antigo.close();
        }
    }

    private SqsClient criarClient(String endpoint, String region) {

        return SqsClient.builder()
                .endpointOverride(URI.create(endpoint))
                .region(Region.of(region))
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create("test", "test")
                        )
                )
                .build();
    }

    private void validar(String endpoint, String region) {

        if (endpoint == null || endpoint.isBlank()) {
            throw new IllegalArgumentException("Endpoint inválido");
        }

        if (region == null || region.isBlank()) {
            throw new IllegalArgumentException("Region inválida");
        }
    }
    
    public String montarQueueUrl(String fila) {
        return "%s/%s/%s".formatted(endpoint, ACCOUNT_ID, fila);
    }

    public SqsClient getClient() {
        return clientRef.get();
    }

    public String getRegion() {
        return region;
    }

    public String getEndpoint() {
        return endpoint;
    }
}