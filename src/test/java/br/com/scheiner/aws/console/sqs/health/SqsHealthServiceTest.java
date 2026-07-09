package br.com.scheiner.aws.console.sqs.health;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.ListQueuesResponse;
import software.amazon.awssdk.services.sqs.model.SqsException;

class SqsHealthServiceTest {

	private final SqsHealthService service = new SqsHealthService();

	@Test
	@DisplayName("Deve retornar conectado quando listar filas com sucesso")
	void deve_retornar_conectado_quando_listar_filas_com_sucesso() {
		var client = mock(SqsClient.class);
		when(client.listQueues()).thenReturn(ListQueuesResponse.builder().build());

		assertThat(this.service.isConectado(client)).isTrue();
	}

	@Test
	@DisplayName("Deve retornar desconectado quando o SQS lancar excecao")
	void deve_retornar_desconectado_quando_sqs_lancar_excecao() {
		var client = mock(SqsClient.class);
		when(client.listQueues()).thenThrow(SqsException.builder().message("erro").build());

		assertThat(this.service.isConectado(client)).isFalse();
	}
}
