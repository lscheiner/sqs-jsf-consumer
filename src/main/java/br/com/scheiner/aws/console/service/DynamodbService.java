package br.com.scheiner.aws.console.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import br.com.scheiner.aws.console.config.DynamoDbClientProvider;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.ListTablesRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import software.amazon.awssdk.services.dynamodb.model.TableDescription;

@Service
public class DynamodbService {
	
    private final DynamoDbClientProvider dynamoDbClientProvider;

	public DynamodbService(DynamoDbClientProvider dynamoDbClientProvider) {
		super();
		this.dynamoDbClientProvider = dynamoDbClientProvider;
	}
    
	public List<String> buscarTabelas() {

		 var response = dynamoDbClientProvider
                 .getClient()
                 .listTables(
                         ListTablesRequest.builder().build()
                 );

         return response.tableNames();
    }

	public TableDescription descreverTabela(String nomeTabela) {
		return dynamoDbClientProvider
				.getClient()
				.describeTable(
						DescribeTableRequest.builder()
								.tableName(nomeTabela)
								.build()
				)
				.table();
	}

	public List<Map<String, AttributeValue>> buscarItens(String nomeTabela) {
		var response = dynamoDbClientProvider
				.getClient()
				.scan(
						ScanRequest.builder()
								.tableName(nomeTabela)
								.build()
				);

		return response.items();
	}

	public void salvarItem(String nomeTabela, Map<String, AttributeValue> item) {
		dynamoDbClientProvider
				.getClient()
				.putItem(
						PutItemRequest.builder()
								.tableName(nomeTabela)
								.item(item)
								.build()
				);
	}

	public Map<String, AttributeValue> buscarItem(String nomeTabela, Map<String, AttributeValue> chave) {
		return dynamoDbClientProvider
				.getClient()
				.getItem(
						GetItemRequest.builder()
								.tableName(nomeTabela)
								.key(chave)
								.build()
				)
				.item();
	}

	public void excluirItem(String nomeTabela, Map<String, AttributeValue> chave) {
		dynamoDbClientProvider
				.getClient()
				.deleteItem(
						DeleteItemRequest.builder()
								.tableName(nomeTabela)
								.key(chave)
								.build()
				);
	}

}
