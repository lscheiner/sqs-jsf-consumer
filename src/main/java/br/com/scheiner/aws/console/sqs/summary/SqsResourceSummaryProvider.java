package br.com.scheiner.aws.console.sqs.summary;

import org.springframework.stereotype.Service;

import br.com.scheiner.aws.console.resource.model.ResourceDescriptor;
import br.com.scheiner.aws.console.resource.model.ResourceSnapshot;
import br.com.scheiner.aws.console.resource.model.ResourceType;
import br.com.scheiner.aws.console.resource.model.ServiceStatus;
import br.com.scheiner.aws.console.resource.provider.ResourceSummaryProvider;
import br.com.scheiner.aws.console.sqs.service.SqsExplorerService;

@Service
public class SqsResourceSummaryProvider implements ResourceSummaryProvider {

	private final SqsExplorerService sqsExplorerService;

	public SqsResourceSummaryProvider(SqsExplorerService sqsExplorerService) {
		this.sqsExplorerService = sqsExplorerService;
	}

	@Override
	public ResourceType getType() {
		return ResourceType.SQS;
	}

	@Override
	public ResourceSnapshot load() {
		var queues = this.sqsExplorerService.listarFilas();
		var snapshot = new ResourceSnapshot();
		snapshot.setType(this.getType());
		snapshot.setStatus(ServiceStatus.CONNECTED);
		snapshot.setCount(queues.size());
		snapshot.setResources(queues.stream()
				.map(queue -> new ResourceDescriptor(this.getType(), queue.getNome(), queue.getNome()))
				.toList());
		return snapshot;
	}
}
