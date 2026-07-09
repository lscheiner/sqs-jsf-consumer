package br.com.scheiner.aws.console.dynamodb.summary;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.com.scheiner.aws.console.dynamodb.service.DynamoDbService;
import br.com.scheiner.aws.console.resource.model.ResourceType;
import br.com.scheiner.aws.console.resource.model.ServiceStatus;

class DynamoDbResourceInfoProviderTest {

	@Test
	@DisplayName("Deve transformar tabelas DynamoDB em recursos do dashboard")
	void deve_transformar_tabelas_dynamodb_em_recursos_do_dashboard() {
		var service = mock(DynamoDbService.class);
		when(service.buscarTabelas()).thenReturn(List.of("Orders", "Customers"));

		var info = new DynamoDbResourceInfoProvider(service).load();

		assertThat(info.getType()).isEqualTo(ResourceType.DYNAMODB);
		assertThat(info.getStatus()).isEqualTo(ServiceStatus.CONNECTED);
		assertThat(info.getCount()).isEqualTo(2);
		assertThat(info.getResources()).extracting("name").containsExactly("Orders", "Customers");
		assertThat(info.getResources()).extracting("identifier").containsExactly("Orders", "Customers");
	}
}
