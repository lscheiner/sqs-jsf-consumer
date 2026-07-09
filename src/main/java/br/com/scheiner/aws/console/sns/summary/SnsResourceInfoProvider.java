package br.com.scheiner.aws.console.sns.summary;

import org.springframework.stereotype.Service;

import br.com.scheiner.aws.console.resource.model.ResourceDescriptor;
import br.com.scheiner.aws.console.resource.model.ResourceInfo;
import br.com.scheiner.aws.console.resource.model.ResourceType;
import br.com.scheiner.aws.console.resource.model.ServiceStatus;
import br.com.scheiner.aws.console.resource.provider.ResourceInfoProvider;
import br.com.scheiner.aws.console.sns.service.SnsService;

@Service
public class SnsResourceInfoProvider implements ResourceInfoProvider {

	private final SnsService snsService;

	public SnsResourceInfoProvider(SnsService snsService) {
		this.snsService = snsService;
	}

	@Override
	public ResourceType getType() {
		return ResourceType.SNS;
	}

	@Override
	public ResourceInfo load() {
		var topics = this.snsService.listarTopicos();
		var resourceInfo = new ResourceInfo();
		resourceInfo.setType(this.getType());
		resourceInfo.setStatus(ServiceStatus.CONNECTED);
		resourceInfo.setCount(topics.size());
		resourceInfo.setResources(topics.stream()
				.map(topic -> new ResourceDescriptor(
						this.getType(),
						topic.getNome(),
						topic.getArn()))
				.toList());
		return resourceInfo;
	}
}
