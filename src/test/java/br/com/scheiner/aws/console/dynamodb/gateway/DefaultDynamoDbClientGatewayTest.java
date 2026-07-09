package br.com.scheiner.aws.console.dynamodb.gateway;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.com.scheiner.aws.console.configuration.aws.AwsConfiguration;
import br.com.scheiner.aws.console.dynamodb.health.DynamoDbHealthService;

class DefaultDynamoDbClientGatewayTest {

	@Test
	@DisplayName("Deve criar e recriar client DynamoDB")
	void deve_criar_e_recriar_client_dynamodb() {
		var configuration = new AwsConfiguration("http://localhost:4566", "sa-east-1");
		var gateway = new DefaultDynamoDbClientGateway(configuration, mock(DynamoDbHealthService.class));
		var clientAnterior = gateway.getClient();

		configuration.setEndpoint("http://localhost:4567");
		gateway.reconfigurar();

		assertThat(clientAnterior).isNotNull();
		assertThat(gateway.getClient()).isNotSameAs(clientAnterior);
	}

	@Test
	@DisplayName("Deve refletir resultado do health check do DynamoDB")
	void deve_refletir_resultado_do_health_check_do_dynamodb() {
		var health = mock(DynamoDbHealthService.class);
		var gateway = new DefaultDynamoDbClientGateway(new AwsConfiguration("http://localhost:4566", "sa-east-1"), health);
		when(health.isConectado(gateway)).thenReturn(true);

		assertThat(gateway.isConectado()).isTrue();
	}
}
