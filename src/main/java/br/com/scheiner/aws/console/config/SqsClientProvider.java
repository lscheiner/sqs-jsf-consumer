package br.com.scheiner.aws.console.config;

import java.net.URI;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;

@Component
public class SqsClientProvider implements AwsProvider {

	private static final Logger LOGGER = LoggerFactory.getLogger(SqsClientProvider.class);

    public static final String ACCOUNT_ID = "000000000000";

    private final AtomicReference<SqsClient> clientRef = new AtomicReference<>();
    
    private final AwsConfiguration awsConfiguration;

    public SqsClientProvider(AwsConfiguration awsConfiguration ) {
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
            this.getClient().listQueues();
            return true;
        } catch (Exception ex) {
        	LOGGER.error("Erro conectando no SQS" ,ex );
            return false;
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

    public String montarQueueUrl(String fila) {

        return "%s/%s/%s".formatted(
        		this.awsConfiguration.getEndpoint(),
                ACCOUNT_ID,
                fila);
    }
    
    public String extrairNomeFila(String queueUrl) {
        return queueUrl.substring(queueUrl.lastIndexOf("/") + 1);
    }

    public SqsClient getClient() {
        return this.clientRef.get();
    }

}
