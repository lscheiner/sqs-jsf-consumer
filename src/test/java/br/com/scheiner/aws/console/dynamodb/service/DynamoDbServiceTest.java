package br.com.scheiner.aws.console.dynamodb.service;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.com.scheiner.aws.console.dynamodb.gateway.DynamoDbClientGateway;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemResponse;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableRequest;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableResponse;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.ListTablesRequest;
import software.amazon.awssdk.services.dynamodb.model.ListTablesResponse;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemResponse;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanResponse;
import software.amazon.awssdk.services.dynamodb.model.TableDescription;

class DynamoDbServiceTest {

	private final DynamoDbClient client = mock(DynamoDbClient.class);
	private final DynamoDbClientGateway gateway = () -> this.client;
	private final DynamoDbService service = new DynamoDbService(this.gateway);

	@Test
	@DisplayName("Deve listar tabelas retornadas pelo DynamoDB")
	void deve_listar_tabelas_retornadas_pelo_dynamodb() {
		when(this.client.listTables(any(ListTablesRequest.class)))
				.thenReturn(ListTablesResponse.builder().tableNames("Orders", "Customers").build());

		assertThat(this.service.buscarTabelas()).containsExactly("Orders", "Customers");
	}

	@Test
	@DisplayName("Deve retornar descricao da tabela selecionada")
	void deve_retornar_descricao_da_tabela_selecionada() {
		var descricao = TableDescription.builder().tableName("Orders").build();
		when(this.client.describeTable(any(DescribeTableRequest.class)))
				.thenReturn(DescribeTableResponse.builder().table(descricao).build());

		assertThat(this.service.descreverTabela("Orders")).isSameAs(descricao);
	}

	@Test
	@DisplayName("Deve buscar itens da tabela usando scan")
	void deve_buscar_itens_da_tabela_usando_scan() {
	    var item = Map.of(
	            "id", AttributeValue.builder().s("1").build());

	    when(this.client.scan(any(ScanRequest.class)))
	            .thenReturn(
	                    ScanResponse.builder()
	                            .items(List.of(item))
	                            .build());

	    assertThat(this.service.buscarItens("Orders"))
	            .containsExactly(item);
	}

	@Test
	@DisplayName("Deve salvar item e retornar sem excecao quando o put for aceito")
	void deve_salvar_item_e_retornar_sem_excecao_quando_put_for_aceito() {
		when(this.client.putItem(any(PutItemRequest.class))).thenReturn(PutItemResponse.builder().build());

		this.service.salvarItem("Orders", Map.of("id", AttributeValue.builder().s("1").build()));
	}

	@Test
	@DisplayName("Deve buscar item pela chave informada")
	void deve_buscar_item_pela_chave_informada() {
		var item = Map.of("id", AttributeValue.builder().s("1").build());
		when(this.client.getItem(any(GetItemRequest.class)))
				.thenReturn(GetItemResponse.builder().item(item).build());

		assertThat(this.service.buscarItem("Orders", item)).isEqualTo(item);
	}

	@Test
	@DisplayName("Deve excluir item pela chave informada sem gerar erro")
	void deve_excluir_item_pela_chave_informada_sem_gerar_erro() {
		when(this.client.deleteItem(any(DeleteItemRequest.class))).thenReturn(DeleteItemResponse.builder().build());

		this.service.excluirItem("Orders", Map.of("id", AttributeValue.builder().s("1").build()));
	}
}
