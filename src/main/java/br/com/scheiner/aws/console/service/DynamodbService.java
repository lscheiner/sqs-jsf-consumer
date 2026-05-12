package br.com.scheiner.aws.console.service;

import java.util.List;

import org.springframework.stereotype.Service;

import br.com.scheiner.aws.console.config.DynamoDbClientProvider;
import software.amazon.awssdk.services.dynamodb.model.ListTablesRequest;

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

}
