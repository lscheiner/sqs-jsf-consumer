package br.com.scheiner.aws.console.redis.summary;

import java.util.List;

import org.springframework.stereotype.Service;

import br.com.scheiner.aws.console.redis.service.RedisService;
import br.com.scheiner.aws.console.resource.model.ResourceDescriptor;
import br.com.scheiner.aws.console.resource.model.ResourceInfo;
import br.com.scheiner.aws.console.resource.model.ResourceType;
import br.com.scheiner.aws.console.resource.model.ServiceStatus;
import br.com.scheiner.aws.console.resource.provider.ResourceSummaryProvider;

@Service
public class RedisResourceSummaryProvider implements ResourceSummaryProvider {

	private final RedisService redisService;

	public RedisResourceSummaryProvider(RedisService redisService) {
		this.redisService = redisService;
	}

	@Override
	public ResourceType getType() {
		return ResourceType.REDIS;
	}

	@Override
	public ResourceInfo load() {
		var configuration = this.redisService.carregarConfiguracao();
		var address = "%s:%d".formatted(configuration.getHost(), configuration.getPort());
		var connected = this.redisService.testarConexao();
		var resourceInfo = new ResourceInfo();
		resourceInfo.setType(this.getType());
		resourceInfo.setStatus(connected ? ServiceStatus.CONNECTED : ServiceStatus.DISCONNECTED);
		resourceInfo.setConfiguredAddress(address);
		resourceInfo.setCount(connected ? this.redisService.contarChaves() : 0L);
		resourceInfo.setResources(List.of(new ResourceDescriptor(this.getType(), address, address)));
		return resourceInfo;
	}
}
