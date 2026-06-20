package br.com.scheiner.aws.console.dynamodb.summary;

import org.springframework.stereotype.Service;

import br.com.scheiner.aws.console.dynamodb.service.DynamoDbService;
import br.com.scheiner.aws.console.resource.model.ResourceDescriptor;
import br.com.scheiner.aws.console.resource.model.ResourceSnapshot;
import br.com.scheiner.aws.console.resource.model.ResourceType;
import br.com.scheiner.aws.console.resource.model.ServiceStatus;
import br.com.scheiner.aws.console.resource.provider.ResourceSummaryProvider;

@Service
public class DynamoDbResourceSummaryProvider implements ResourceSummaryProvider {

	private final DynamoDbService dynamodbService;

	public DynamoDbResourceSummaryProvider(DynamoDbService dynamodbService) {
		this.dynamodbService = dynamodbService;
	}

	@Override
	public ResourceType getType() {
		return ResourceType.DYNAMODB;
	}

	@Override
	public ResourceSnapshot load() {
		var tables = this.dynamodbService.buscarTabelas();
		var snapshot = new ResourceSnapshot();
		snapshot.setType(this.getType());
		snapshot.setStatus(ServiceStatus.CONNECTED);
		snapshot.setCount(tables.size());
		snapshot.setResources(tables.stream()
				.map(table -> new ResourceDescriptor(this.getType(), table, table))
				.toList());
		return snapshot;
	}
}
