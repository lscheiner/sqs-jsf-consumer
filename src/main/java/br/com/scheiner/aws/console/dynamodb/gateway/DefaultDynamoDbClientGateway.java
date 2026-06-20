package br.com.scheiner.aws.console.dynamodb.gateway;

import java.net.URI;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.stereotype.Component;

import br.com.scheiner.aws.console.configuration.aws.AwsConfiguration;
import br.com.scheiner.aws.console.configuration.aws.AwsProvider;
import br.com.scheiner.aws.console.dynamodb.health.DynamoDbHealthService;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

@Component
public class DefaultDynamoDbClientGateway implements DynamoDbClientGateway, AwsProvider {

	private final AtomicReference<DynamoDbClient> clientRef = new AtomicReference<>();

	private final AwsConfiguration awsConfiguration;

	private final DynamoDbHealthService dynamoDbHealthService;

	public DefaultDynamoDbClientGateway(
			AwsConfiguration awsConfiguration,
			DynamoDbHealthService dynamoDbHealthService) {

		this.awsConfiguration = awsConfiguration;
		this.dynamoDbHealthService = dynamoDbHealthService;
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
		return this.dynamoDbHealthService.isConectado(this);
	}

	@Override
	public DynamoDbClient getClient() {
		return this.clientRef.get();
	}

	private DynamoDbClient criarClient(String endpoint, Region region) {
		return DynamoDbClient.builder()
				.endpointOverride(URI.create(endpoint))
				.region(region)
				.credentialsProvider(
						StaticCredentialsProvider.create(
								AwsBasicCredentials.create("test", "test")))
				.build();
	}
}
