package br.com.scheiner.aws.console.dashboard.service;

import java.util.EnumMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import br.com.scheiner.aws.console.configuration.aws.AwsConfiguration;
import br.com.scheiner.aws.console.dashboard.model.DashboardData;
import br.com.scheiner.aws.console.dashboard.model.DashboardSummary;
import br.com.scheiner.aws.console.dashboard.model.ResourceNode;
import br.com.scheiner.aws.console.resource.model.ResourceInfo;
import br.com.scheiner.aws.console.resource.model.ResourceType;
import br.com.scheiner.aws.console.resource.model.ServiceStatus;
import br.com.scheiner.aws.console.resource.provider.ResourceInfoProvider;

@Service
public class DashboardService {

	private static final Logger LOGGER = LoggerFactory.getLogger(DashboardService.class);

	private final List<ResourceInfoProvider> providers;
	private final AwsConfiguration awsConfiguration;

	public DashboardService(List<ResourceInfoProvider> providers, AwsConfiguration awsConfiguration) {
		this.providers = providers;
		this.awsConfiguration = awsConfiguration;
	}

	public DashboardData load() {
		
		var resourceInfos = new EnumMap<ResourceType, ResourceInfo>(ResourceType.class);

		this.providers.forEach(provider -> resourceInfos.put(provider.getType(), this.load(provider)));

		var data = new DashboardData();

		var resources = new EnumMap<ResourceType, List<ResourceNode>>(ResourceType.class);

		resourceInfos.forEach((type, resourceInfo) ->
				resources.put(type,
						resourceInfo.getResources().stream()
								.map(resource -> 
								ResourceNode.resource(
										resource.getType(),
										resource.getName(),
										resource.getIdentifier()))
								.toList()));

		data.setResources(resources);
		data.setSummary(this.buildSummary(resourceInfos));

		return data;
	}

	private ResourceInfo load(ResourceInfoProvider provider) {
		try {
			return provider.load();
		} catch (Exception exception) {
			LOGGER.warn("Nao foi possivel consultar {} para o Dashboard", provider.getType(), exception);
			var unavailable = new ResourceInfo();
			unavailable.setType(provider.getType());
			unavailable.setStatus(ServiceStatus.UNAVAILABLE);
			return unavailable;
		}
	}

	private DashboardSummary buildSummary(EnumMap<ResourceType, ResourceInfo> resourceInfos) {
		
		var sqs = this.getResourceInfo(resourceInfos, ResourceType.SQS);
		var dynamodb = this.getResourceInfo(resourceInfos, ResourceType.DYNAMODB);
		var redis = this.getResourceInfo(resourceInfos, ResourceType.REDIS);
		var sns = this.getResourceInfo(resourceInfos, ResourceType.SNS);
		
		var summary = new DashboardSummary();
		summary.setSqsQueueCount(sqs.getCountAsInt());
		summary.setDynamoTableCount(dynamodb.getCountAsInt());
		summary.setSnsTopicCount(sns.getCountAsInt());
		summary.setRedisKeyCount(redis.getCountAsInt());
		summary.setLocalstackStatus(this.localstackStatus(sqs, dynamodb));
		summary.setRedisStatus(redis.getStatus());
		summary.setLocalstackEndpoint(this.awsConfiguration.getEndpoint());
		summary.setRedisHost(redis.getConfiguredAddress());
		return summary;
	}

	private ResourceInfo getResourceInfo(
			EnumMap<ResourceType, ResourceInfo> resourceInfos,
			ResourceType type) {

		return resourceInfos.computeIfAbsent(type, ignored -> {
			var resourceInfo = new ResourceInfo();
			resourceInfo.setType(type);
			resourceInfo.setStatus(ServiceStatus.UNAVAILABLE);
			return resourceInfo;
		});
	}

	private ServiceStatus localstackStatus(
			ResourceInfo sqs, ResourceInfo dynamodb) {
		if (sqs.getStatus() == ServiceStatus.CONNECTED || dynamodb.getStatus() == ServiceStatus.CONNECTED) {
			return ServiceStatus.CONNECTED;
		}
		if (sqs.getStatus() == ServiceStatus.DISCONNECTED || dynamodb.getStatus() == ServiceStatus.DISCONNECTED) {
			return ServiceStatus.DISCONNECTED;
		}
		return ServiceStatus.UNAVAILABLE;
	}
}
