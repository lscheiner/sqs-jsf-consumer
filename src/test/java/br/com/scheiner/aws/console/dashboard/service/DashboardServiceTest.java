package br.com.scheiner.aws.console.dashboard.service;


import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.com.scheiner.aws.console.configuration.aws.AwsConfiguration;
import br.com.scheiner.aws.console.resource.model.ResourceDescriptor;
import br.com.scheiner.aws.console.resource.model.ResourceInfo;
import br.com.scheiner.aws.console.resource.model.ResourceType;
import br.com.scheiner.aws.console.resource.model.ServiceStatus;
import br.com.scheiner.aws.console.resource.provider.ResourceInfoProvider;

class DashboardServiceTest {

	@Test
	@DisplayName("Deve agregar recursos e resumo a partir dos providers disponiveis")
	void deve_agregar_recursos_e_resumo_a_partir_dos_providers_disponiveis() {
		var service = new DashboardService(List.of(
				provider(ResourceType.SQS, ServiceStatus.CONNECTED, 2, null,
						new ResourceDescriptor(ResourceType.SQS, "fila-a", "url-a")),
				provider(ResourceType.DYNAMODB, ServiceStatus.DISCONNECTED, 1, null,
						new ResourceDescriptor(ResourceType.DYNAMODB, "Orders", "Orders")),
				provider(ResourceType.SNS, ServiceStatus.CONNECTED, 1, null,
						new ResourceDescriptor(ResourceType.SNS, "topico", "arn")),
				provider(ResourceType.REDIS, ServiceStatus.CONNECTED, 3, "localhost:6379",
						new ResourceDescriptor(ResourceType.REDIS, "chave", "chave"))),
				new AwsConfiguration("http://localhost:4566", "sa-east-1"));

		var data = service.load();

		assertThat(data.getSummary().getSqsQueueCount()).isEqualTo(2);
		assertThat(data.getSummary().getDynamoTableCount()).isEqualTo(1);
		assertThat(data.getSummary().getSnsTopicCount()).isEqualTo(1);
		assertThat(data.getSummary().getRedisKeyCount()).isEqualTo(3);
		assertThat(data.getSummary().getLocalstackStatus()).isEqualTo(ServiceStatus.CONNECTED);
		assertThat(data.getSummary().getRedisStatus()).isEqualTo(ServiceStatus.CONNECTED);
		assertThat(data.getSummary().getLocalstackEndpoint()).isEqualTo("http://localhost:4566");
		assertThat(data.getSummary().getRedisHost()).isEqualTo("localhost:6379");
		var node = data.getResources(ResourceType.SQS).getFirst();
		assertThat(data.getResources(ResourceType.SQS)).hasSize(1);
		assertThat(node.getType()).isEqualTo(ResourceType.SQS);
		assertThat(node.getName()).isEqualTo("fila-a");
		assertThat(node.getIdentifier()).isEqualTo("url-a");
		assertThat(node.isResource()).isTrue();
	}

	@Test
	@DisplayName("Deve marcar recurso como indisponivel quando o provider falhar")
	void deve_marcar_recurso_como_indisponivel_quando_provider_falhar() {
		var providerFalhando = new ResourceInfoProvider() {
			@Override
			public ResourceType getType() {
				return ResourceType.SQS;
			}

			@Override
			public ResourceInfo load() {
				throw new IllegalStateException("servico fora");
			}
		};
		var service = new DashboardService(List.of(providerFalhando), new AwsConfiguration("http://local", "sa-east-1"));

		var data = service.load();

		assertThat(data.getSummary().getSqsQueueCount()).isZero();
		assertThat(data.getSummary().getLocalstackStatus()).isEqualTo(ServiceStatus.UNAVAILABLE);
		assertThat(data.getResources(ResourceType.SQS)).isEmpty();
	}

	@Test
	@DisplayName("Deve considerar LocalStack desconectado quando algum provider AWS estiver desconectado e nenhum estiver conectado")
	void deve_considerar_localstack_desconectado_quando_algum_provider_aws_estiver_desconectado_e_nenhum_estiver_conectado() {
		var service = new DashboardService(List.of(
				provider(ResourceType.SQS, ServiceStatus.DISCONNECTED, 0, null),
				provider(ResourceType.DYNAMODB, ServiceStatus.UNAVAILABLE, 0, null),
				provider(ResourceType.SNS, ServiceStatus.UNAVAILABLE, 0, null)),
				new AwsConfiguration("http://local", "sa-east-1"));

		var data = service.load();

		assertThat(data.getSummary().getLocalstackStatus()).isEqualTo(ServiceStatus.DISCONNECTED);
	}

	private static ResourceInfoProvider provider(
			ResourceType type,
			ServiceStatus status,
			long count,
			String configuredAddress,
			ResourceDescriptor... resources) {

		return new ResourceInfoProvider() {
			@Override
			public ResourceType getType() {
				return type;
			}

			@Override
			public ResourceInfo load() {
				var info = new ResourceInfo();
				info.setType(type);
				info.setStatus(status);
				info.setCount(count);
				info.setConfiguredAddress(configuredAddress);
				info.setResources(List.of(resources));
				return info;
			}
		};
	}
}
