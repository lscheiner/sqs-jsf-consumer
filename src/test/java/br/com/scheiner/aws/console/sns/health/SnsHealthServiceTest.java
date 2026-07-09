package br.com.scheiner.aws.console.sns.health;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.com.scheiner.aws.console.sns.gateway.SnsClientGateway;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.ListTopicsRequest;
import software.amazon.awssdk.services.sns.model.ListTopicsResponse;
import software.amazon.awssdk.services.sns.model.SnsException;

class SnsHealthServiceTest {

	private final SnsHealthService service = new SnsHealthService();

	@Test
	@DisplayName("Deve retornar conectado quando listar topicos com sucesso")
	void deve_retornar_conectado_quando_listar_topicos_com_sucesso() {
		var client = mock(SnsClient.class);
		when(client.listTopics(any(ListTopicsRequest.class))).thenReturn(ListTopicsResponse.builder().build());

		assertThat(this.service.isConectado(() -> client)).isTrue();
	}

	@Test
	@DisplayName("Deve retornar desconectado quando o SNS lancar excecao")
	void deve_retornar_desconectado_quando_sns_lancar_excecao() {
		var client = mock(SnsClient.class);
		var gateway = (SnsClientGateway) () -> client;
		when(client.listTopics(any(ListTopicsRequest.class))).thenThrow(SnsException.builder().message("erro").build());

		assertThat(this.service.isConectado(gateway)).isFalse();
	}
}
