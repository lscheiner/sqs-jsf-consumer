package br.com.scheiner.aws.console.dynamodb.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import br.com.scheiner.aws.console.dynamodb.gateway.DynamoDbClientGateway;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.ListTablesRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import software.amazon.awssdk.services.dynamodb.model.TableDescription;

@Service
public class DynamoDbService {

	private final DynamoDbClientGateway dynamoDbClientGateway;

	public DynamoDbService(DynamoDbClientGateway dynamoDbClientGateway) {
		this.dynamoDbClientGateway = dynamoDbClientGateway;
	}

	public List<String> buscarTabelas() {
		return this.dynamoDbClientGateway
				.getClient()
				.listTables(ListTablesRequest.builder().build())
				.tableNames();
	}

	public TableDescription descreverTabela(String nomeTabela) {
		return this.dynamoDbClientGateway
				.getClient()
				.describeTable(DescribeTableRequest.builder()
						.tableName(nomeTabela)
						.build())
				.table();
	}

	public List<Map<String, AttributeValue>> buscarItens(String nomeTabela) {
		return this.dynamoDbClientGateway
				.getClient()
				.scan(ScanRequest.builder()
						.tableName(nomeTabela)
						.build())
				.items();
	}

	public void salvarItem(String nomeTabela, Map<String, AttributeValue> item) {
		this.dynamoDbClientGateway
				.getClient()
				.putItem(PutItemRequest.builder()
						.tableName(nomeTabela)
						.item(item)
						.build());
	}

	public Map<String, AttributeValue> buscarItem(String nomeTabela, Map<String, AttributeValue> chave) {
		return this.dynamoDbClientGateway
				.getClient()
				.getItem(GetItemRequest.builder()
						.tableName(nomeTabela)
						.key(chave)
						.build())
				.item();
	}

	public void excluirItem(String nomeTabela, Map<String, AttributeValue> chave) {
		this.dynamoDbClientGateway
				.getClient()
				.deleteItem(DeleteItemRequest.builder()
						.tableName(nomeTabela)
						.key(chave)
						.build());
	}
}
