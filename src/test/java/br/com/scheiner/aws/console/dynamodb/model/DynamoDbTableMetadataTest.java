package br.com.scheiner.aws.console.dynamodb.model;



import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.KeyType;
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType;
import software.amazon.awssdk.services.dynamodb.model.TableDescription;
import software.amazon.awssdk.services.dynamodb.model.TableStatus;

class DynamoDbTableMetadataTest {

	@Test
	@DisplayName("Deve extrair chaves, tipos, quantidade e status da tabela")
	void deve_extrair_chaves_tipos_quantidade_e_status_da_tabela() {
		var descricao = TableDescription.builder()
				.tableName("Orders")
				.itemCount(15L)
				.tableStatus(TableStatus.ACTIVE)
				.attributeDefinitions(
						AttributeDefinition.builder().attributeName("orderId").attributeType(ScalarAttributeType.S).build(),
						AttributeDefinition.builder().attributeName("createdAt").attributeType(ScalarAttributeType.N).build())
				.keySchema(
						KeySchemaElement.builder().attributeName("orderId").keyType(KeyType.HASH).build(),
						KeySchemaElement.builder().attributeName("createdAt").keyType(KeyType.RANGE).build())
				.build();

		var metadata = new DynamoDbTableMetadata(descricao);

		assertThat(metadata.getTableDescription()).isSameAs(descricao);
		assertThat(metadata.getPartitionKey()).isEqualTo("orderId");
		assertThat(metadata.getSortKey()).isEqualTo("createdAt");
		assertThat(metadata.getTipoPartitionKey()).isEqualTo("S");
		assertThat(metadata.getTipoSortKey()).isEqualTo("N");
		assertThat(metadata.getTipoAtributo("inexistente")).isNull();
		assertThat(metadata.getQuantidadeItens()).isEqualTo(15L);
		assertThat(metadata.getStatusTabela()).isEqualTo("ACTIVE");
	}

	@Test
	@DisplayName("Deve retornar nulo para sort key quando a tabela possui apenas partition key")
	void deve_retornar_nulo_para_sort_key_quando_tabela_possui_apenas_partition_key() {
		var descricao = TableDescription.builder()
				.attributeDefinitions(AttributeDefinition.builder().attributeName("id").attributeType(ScalarAttributeType.S).build())
				.keySchema(KeySchemaElement.builder().attributeName("id").keyType(KeyType.HASH).build())
				.build();

		var metadata = new DynamoDbTableMetadata(descricao);

		assertThat(metadata.getPartitionKey()).isEqualTo("id");
		assertThat(metadata.getSortKey()).isNull();
		assertThat(metadata.getTipoSortKey()).isNull();
	}
}
