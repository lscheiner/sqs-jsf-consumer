package br.com.scheiner.aws.console.config;

import java.net.URI;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.ListTablesRequest;

@Component
public class DynamoDbClientProvider implements AwsProvider {

	private static final Logger LOGGER = LoggerFactory.getLogger(DynamoDbClientProvider.class);

    private final AtomicReference<DynamoDbClient> clientRef = new AtomicReference<>();
    
    private final AwsConfiguration awsConfiguration;

    public DynamoDbClientProvider(AwsConfiguration awsConfiguration) {

    	this.awsConfiguration = awsConfiguration;
    	this.reconfigurar();
    }

    @Override
    public void reconfigurar() {

        var novoClient = this.criarClient(this.awsConfiguration.getEndpoint(), this.awsConfiguration.getRegion());
        var antigo = this.clientRef.getAndSet(novoClient);

        if (antigo != null) {
            antigo.close();
        }
    }
    
    @Override
    public boolean isConectado() {
        try {
            this.getClient().listTables(
                    ListTablesRequest.builder()
                            .limit(1)
                            .build()
            );
            return true;
        } catch (Exception ex) {
            LOGGER.error("Erro conectando no DynamoDB", ex);
            return false;
        }
    }

    private DynamoDbClient criarClient(String endpoint, Region region) {

        return DynamoDbClient.builder()
                .endpointOverride(URI.create(endpoint))
                .region(region)
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(
                                        "test",
                                        "test"
                                )
                        )
                )
                .build();
    }

    public DynamoDbClient getClient() {
        return this.clientRef.get();
    }
}
