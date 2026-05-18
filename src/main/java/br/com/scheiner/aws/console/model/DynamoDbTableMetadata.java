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
		return this.tableDescription;
	}

	public String getPartitionKey() {
		return this.buscarNomeChave(KeyType.HASH);
	}

	public String getSortKey() {
		return this.buscarNomeChave(KeyType.RANGE);
	}

	public String getTipoPartitionKey() {
		return this.buscarTipoChave(KeyType.HASH);
	}

	public String getTipoSortKey() {
		return this.buscarTipoChave(KeyType.RANGE);
	}

	public String getTipoAtributo(String nomeAtributo) {
		return this.tableDescription.attributeDefinitions()
				.stream()
				.filter(atributo -> atributo.attributeName().equals(nomeAtributo))
				.map(AttributeDefinition::attributeTypeAsString)
				.findFirst()
				.orElse(null);
	}

	public Long getQuantidadeItens() {
		return this.tableDescription.itemCount();
	}

	public String getStatusTabela() {
		return this.tableDescription.tableStatusAsString();
	}

	private String buscarNomeChave(KeyType tipoChave) {
		return this.tableDescription.keySchema()
				.stream()
				.filter(chave -> chave.keyType() == tipoChave)
				.map(KeySchemaElement::attributeName)
				.findFirst()
				.orElse(null);
	}

	private String buscarTipoChave(KeyType tipoChave) {
		var nomeChave = this.buscarNomeChave(tipoChave);
		if (nomeChave == null) {
			return null;
		}

		return this.getTipoAtributo(nomeChave);
	}
}
