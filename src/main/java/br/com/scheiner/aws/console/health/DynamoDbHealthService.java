package br.com.scheiner.aws.console.health;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import br.com.scheiner.aws.console.dynamodb.DynamoDbClientGateway;
import software.amazon.awssdk.services.dynamodb.model.ListTablesRequest;

@Service
public class DynamoDbHealthService {

	private static final Logger LOGGER = LoggerFactory.getLogger(DynamoDbHealthService.class);

	public boolean isConectado(DynamoDbClientGateway dynamoDbClientGateway) {
		try {
			dynamoDbClientGateway.getClient().listTables(
					ListTablesRequest.builder()
							.limit(1)
							.build());
			return true;
		} catch (Exception ex) {
			LOGGER.error("Erro conectando no DynamoDB", ex);
			return false;
		}
	}
}
