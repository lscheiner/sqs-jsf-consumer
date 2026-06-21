package br.com.scheiner.aws.console.sqs.summary;

import org.springframework.stereotype.Service;

import br.com.scheiner.aws.console.resource.model.ResourceDescriptor;
import br.com.scheiner.aws.console.resource.model.ResourceInfo;
import br.com.scheiner.aws.console.resource.model.ResourceType;
import br.com.scheiner.aws.console.resource.model.ServiceStatus;
import br.com.scheiner.aws.console.resource.provider.ResourceInfoProvider;
import br.com.scheiner.aws.console.sqs.service.SqsExplorerService;

@Service
public class SqsResourceInfoProvider implements ResourceInfoProvider {

	private final SqsExplorerService sqsExplorerService;

	public SqsResourceInfoProvider(SqsExplorerService sqsExplorerService) {
		this.sqsExplorerService = sqsExplorerService;
	}

	@Override
	public ResourceType getType() {
		return ResourceType.SQS;
	}

	@Override
	public ResourceInfo load() {
		var queues = this.sqsExplorerService.listarFilas();
		var resourceInfo = new ResourceInfo();
		resourceInfo.setType(this.getType());
		resourceInfo.setStatus(ServiceStatus.CONNECTED);
		resourceInfo.setCount(queues.size());
		resourceInfo.setResources(queues.stream()
				.map(queue -> new ResourceDescriptor(this.getType(), queue.getNome(), queue.getNome()))
				.toList());
		return resourceInfo;
	}
}
