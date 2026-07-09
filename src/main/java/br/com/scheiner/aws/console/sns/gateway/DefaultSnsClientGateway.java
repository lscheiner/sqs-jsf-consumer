package br.com.scheiner.aws.console.sns.gateway;

import java.net.URI;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.stereotype.Component;

import br.com.scheiner.aws.console.configuration.aws.AwsConfiguration;
import br.com.scheiner.aws.console.configuration.aws.AwsProvider;
import br.com.scheiner.aws.console.sns.health.SnsHealthService;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;

@Component
public class DefaultSnsClientGateway implements SnsClientGateway, AwsProvider {

	private final AtomicReference<SnsClient> clientRef = new AtomicReference<>();

	private final AwsConfiguration awsConfiguration;

	private final SnsHealthService snsHealthService;

	public DefaultSnsClientGateway(
			AwsConfiguration awsConfiguration,
			SnsHealthService snsHealthService) {

		this.awsConfiguration = awsConfiguration;
		this.snsHealthService = snsHealthService;
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
		return this.snsHealthService.isConectado(this);
	}

	@Override
	public SnsClient getClient() {
		return this.clientRef.get();
	}

	private SnsClient criarClient(String endpoint, Region region) {
		return SnsClient.builder()
				.endpointOverride(URI.create(endpoint))
				.region(region)
				.credentialsProvider(
						StaticCredentialsProvider.create(
								AwsBasicCredentials.create("test", "test")))
				.build();
	}
}
