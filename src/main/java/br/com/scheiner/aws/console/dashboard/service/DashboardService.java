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
import br.com.scheiner.aws.console.resource.model.ResourceSnapshot;
import br.com.scheiner.aws.console.resource.model.ResourceType;
import br.com.scheiner.aws.console.resource.model.ServiceStatus;
import br.com.scheiner.aws.console.resource.provider.ResourceSummaryProvider;

@Service
public class DashboardService {

	private static final Logger LOGGER = LoggerFactory.getLogger(DashboardService.class);

	private final List<ResourceSummaryProvider> providers;
	private final AwsConfiguration awsConfiguration;

	public DashboardService(List<ResourceSummaryProvider> providers, AwsConfiguration awsConfiguration) {
		this.providers = providers;
		this.awsConfiguration = awsConfiguration;
	}

	public DashboardData load() {
		var snapshots = new EnumMap<ResourceType, ResourceSnapshot>(ResourceType.class);
		this.providers.forEach(provider -> snapshots.put(provider.getType(), this.load(provider)));

		var data = new DashboardData();
		var resources = new EnumMap<ResourceType, List<ResourceNode>>(ResourceType.class);
		snapshots.forEach((type, snapshot) -> resources.put(type, snapshot.getResources().stream()
				.map(resource -> ResourceNode.resource(
						resource.getType(), resource.getName(), resource.getIdentifier()))
				.toList()));
		data.setResources(resources);
		data.setSummary(this.buildSummary(snapshots));
		return data;
	}

	private ResourceSnapshot load(ResourceSummaryProvider provider) {
		try {
			return provider.load();
		} catch (Exception exception) {
			LOGGER.warn("Nao foi possivel consultar {} para o Dashboard", provider.getType(), exception);
			var unavailable = new ResourceSnapshot();
			unavailable.setType(provider.getType());
			unavailable.setStatus(ServiceStatus.UNAVAILABLE);
			return unavailable;
		}
	}

	private DashboardSummary buildSummary(EnumMap<ResourceType, ResourceSnapshot> snapshots) {
		var sqs = this.getSnapshot(snapshots, ResourceType.SQS);
		var dynamodb = this.getSnapshot(snapshots, ResourceType.DYNAMODB);
		var redis = this.getSnapshot(snapshots, ResourceType.REDIS);
		var sns = this.getSnapshot(snapshots, ResourceType.SNS);
		var summary = new DashboardSummary();
		summary.setSqsQueueCount(Math.toIntExact(sqs.getCount()));
		summary.setDynamoTableCount(Math.toIntExact(dynamodb.getCount()));
		summary.setSnsTopicCount(Math.toIntExact(sns.getCount()));
		summary.setRedisKeyCount(redis.getCount());
		summary.setLocalstackStatus(this.localstackStatus(sqs, dynamodb));
		summary.setRedisStatus(redis.getStatus());
		summary.setLocalstackEndpoint(this.awsConfiguration.getEndpoint());
		summary.setRedisHost(redis.getConfiguredAddress());
		return summary;
	}

	private ResourceSnapshot getSnapshot(
			EnumMap<ResourceType, ResourceSnapshot> snapshots, ResourceType type) {
		return snapshots.computeIfAbsent(type, ignored -> {
			var snapshot = new ResourceSnapshot();
			snapshot.setType(type);
			snapshot.setStatus(ServiceStatus.UNAVAILABLE);
			return snapshot;
		});
	}

	private ServiceStatus localstackStatus(
			ResourceSnapshot sqs, ResourceSnapshot dynamodb) {
		if (sqs.getStatus() == ServiceStatus.CONNECTED || dynamodb.getStatus() == ServiceStatus.CONNECTED) {
			return ServiceStatus.CONNECTED;
		}
		if (sqs.getStatus() == ServiceStatus.DISCONNECTED || dynamodb.getStatus() == ServiceStatus.DISCONNECTED) {
			return ServiceStatus.DISCONNECTED;
		}
		return ServiceStatus.UNAVAILABLE;
	}
}
