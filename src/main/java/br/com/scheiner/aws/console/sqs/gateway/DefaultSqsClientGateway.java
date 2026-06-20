package br.com.scheiner.aws.console.sqs.gateway;

import java.net.URI;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.stereotype.Component;

import br.com.scheiner.aws.console.configuration.aws.AwsConfiguration;
import br.com.scheiner.aws.console.configuration.aws.AwsProvider;
import br.com.scheiner.aws.console.sqs.health.SqsHealthService;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;

@Component
public class DefaultSqsClientGateway implements SqsClientGateway, AwsProvider {

	private final AtomicReference<SqsClient> clientRef = new AtomicReference<>();

	private final AwsConfiguration awsConfiguration;

	private final SqsQueueUrlResolver sqsQueueUrlResolver;

	private final SqsHealthService sqsHealthService;

	public DefaultSqsClientGateway(
			AwsConfiguration awsConfiguration,
			SqsQueueUrlResolver sqsQueueUrlResolver,
			SqsHealthService sqsHealthService) {

		this.awsConfiguration = awsConfiguration;
		this.sqsQueueUrlResolver = sqsQueueUrlResolver;
		this.sqsHealthService = sqsHealthService;
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
		return this.sqsHealthService.isConectado(this.getClient());
	}

	@Override
	public SqsClient getClient() {
		return this.clientRef.get();
	}

	@Override
	public String montarQueueUrl(String fila) {
		return this.sqsQueueUrlResolver.montarQueueUrl(fila);
	}

	@Override
	public String extrairNomeFila(String queueUrl) {
		return this.sqsQueueUrlResolver.extrairNomeFila(queueUrl);
	}

	private SqsClient criarClient(String endpoint, Region region) {
		return SqsClient.builder()
				.endpointOverride(URI.create(endpoint))
				.region(region)
				.credentialsProvider(
						StaticCredentialsProvider.create(
								AwsBasicCredentials.create("test", "test")))
				.build();
	}
}
