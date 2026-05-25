package br.com.scheiner.aws.console.dynamodb;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

public interface DynamoDbClientGateway {

	DynamoDbClient getClient();
}
