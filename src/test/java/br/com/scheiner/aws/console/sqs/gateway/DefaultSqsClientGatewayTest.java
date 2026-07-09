package br.com.scheiner.aws.console.sqs.gateway;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.com.scheiner.aws.console.configuration.aws.AwsConfiguration;
import br.com.scheiner.aws.console.sqs.health.SqsHealthService;

class DefaultSqsClientGatewayTest {

	@Test
	@DisplayName("Deve criar client SQS e delegar resolucao de URL da fila")
	void deve_criar_client_sqs_e_delegar_resolucao_de_url_da_fila() {
		var health = mock(SqsHealthService.class);
		var configuration = new AwsConfiguration("http://localhost:4566", "sa-east-1");
		var resolver = new SqsQueueUrlResolver(configuration);

		var gateway = new DefaultSqsClientGateway(configuration, resolver, health);

		assertThat(gateway.getClient()).isNotNull();
		assertThat(gateway.montarQueueUrl("fila")).isEqualTo("http://localhost:4566/000000000000/fila");
		assertThat(gateway.extrairNomeFila("http://localhost:4566/000000000000/fila")).isEqualTo("fila");
	}

	@Test
	@DisplayName("Deve refletir resultado do health check do SQS")
	void deve_refletir_resultado_do_health_check_do_sqs() {
		var health = mock(SqsHealthService.class);
		var configuration = new AwsConfiguration("http://localhost:4566", "sa-east-1");
		var gateway = new DefaultSqsClientGateway(configuration, new SqsQueueUrlResolver(configuration), health);
		when(health.isConectado(gateway.getClient())).thenReturn(true);

		assertThat(gateway.isConectado()).isTrue();
	}

	@Test
	@DisplayName("Deve recriar client SQS ao reconfigurar")
	void deve_recriar_client_sqs_ao_reconfigurar() {
		var configuration = new AwsConfiguration("http://localhost:4566", "sa-east-1");
		var gateway = new DefaultSqsClientGateway(
				configuration,
				new SqsQueueUrlResolver(configuration),
				mock(SqsHealthService.class));
		var clientAnterior = gateway.getClient();

		configuration.setEndpoint("http://localhost:4567");
		gateway.reconfigurar();

		assertThat(gateway.getClient()).isNotSameAs(clientAnterior);
	}
}
