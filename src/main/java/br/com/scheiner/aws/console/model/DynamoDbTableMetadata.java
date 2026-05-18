package br.com.scheiner.aws.console.model;

import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.KeyType;
import software.amazon.awssdk.services.dynamodb.model.TableDescription;

public class DynamoDbTableMetadata {

	private final TableDescription tableDescription;

	public DynamoDbTableMetadata(TableDescription tableDescription) {
		this.tableDescription = tableDescription;
	}

	public TableDescription getTableDescription() {
		return tableDescription;
	}

	public String getPartitionKey() {
		return buscarNomeChave(KeyType.HASH);
	}

	public String getSortKey() {
		return buscarNomeChave(KeyType.RANGE);
	}

	public String getTipoPartitionKey() {
		return buscarTipoChave(KeyType.HASH);
	}

	public String getTipoSortKey() {
		return buscarTipoChave(KeyType.RANGE);
	}

	public String getTipoAtributo(String nomeAtributo) {
		return tableDescription.attributeDefinitions()
				.stream()
				.filter(atributo -> atributo.attributeName().equals(nomeAtributo))
				.map(AttributeDefinition::attributeTypeAsString)
				.findFirst()
				.orElse(null);
	}

	public Long getQuantidadeItens() {
		return tableDescription.itemCount();
	}

	public String getStatusTabela() {
		return tableDescription.tableStatusAsString();
	}

	private String buscarNomeChave(KeyType tipoChave) {
		return tableDescription.keySchema()
				.stream()
				.filter(chave -> chave.keyType() == tipoChave)
				.map(KeySchemaElement::attributeName)
				.findFirst()
				.orElse(null);
	}

	private String buscarTipoChave(KeyType tipoChave) {
		String nomeChave = buscarNomeChave(tipoChave);
		if (nomeChave == null) {
			return null;
		}

		return getTipoAtributo(nomeChave);
	}
}
