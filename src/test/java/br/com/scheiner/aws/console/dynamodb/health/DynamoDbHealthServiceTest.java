package br.com.scheiner.aws.console.dynamodb.health;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.ListTablesRequest;
import software.amazon.awssdk.services.dynamodb.model.ListTablesResponse;

class DynamoDbHealthServiceTest {

	private final DynamoDbHealthService service = new DynamoDbHealthService();

	@Test
	@DisplayName("Deve retornar conectado quando listar tabelas com sucesso")
	void deve_retornar_conectado_quando_listar_tabelas_com_sucesso() {
		var client = mock(DynamoDbClient.class);
		when(client.listTables(any(ListTablesRequest.class))).thenReturn(ListTablesResponse.builder().build());

		assertThat(this.service.isConectado(() -> client)).isTrue();
	}

	@Test
	@DisplayName("Deve retornar desconectado quando o DynamoDB lancar excecao")
	void deve_retornar_desconectado_quando_dynamodb_lancar_excecao() {
		var client = mock(DynamoDbClient.class);
		when(client.listTables(any(ListTablesRequest.class))).thenThrow(DynamoDbException.builder().message("erro").build());

		assertThat(this.service.isConectado(() -> client)).isFalse();
	}
}
