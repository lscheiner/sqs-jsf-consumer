package br.com.scheiner.aws.console.sns.gateway;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.com.scheiner.aws.console.configuration.aws.AwsConfiguration;
import br.com.scheiner.aws.console.sns.health.SnsHealthService;

class DefaultSnsClientGatewayTest {

	@Test
	@DisplayName("Deve criar e recriar client SNS")
	void deve_criar_e_recriar_client_sns() {
		var configuration = new AwsConfiguration("http://localhost:4566", "sa-east-1");
		var gateway = new DefaultSnsClientGateway(configuration, mock(SnsHealthService.class));
		var clientAnterior = gateway.getClient();

		configuration.setEndpoint("http://localhost:4567");
		gateway.reconfigurar();

		assertThat(clientAnterior).isNotNull();
		assertThat(gateway.getClient()).isNotSameAs(clientAnterior);
	}

	@Test
	@DisplayName("Deve refletir resultado do health check do SNS")
	void deve_refletir_resultado_do_health_check_do_sns() {
		var health = mock(SnsHealthService.class);
		var gateway = new DefaultSnsClientGateway(new AwsConfiguration("http://localhost:4566", "sa-east-1"), health);
		when(health.isConectado(gateway)).thenReturn(true);

		assertThat(gateway.isConectado()).isTrue();
	}
}
