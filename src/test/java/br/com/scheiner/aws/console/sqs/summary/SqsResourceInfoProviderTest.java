package br.com.scheiner.aws.console.sqs.summary;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.com.scheiner.aws.console.resource.model.ResourceType;
import br.com.scheiner.aws.console.resource.model.ServiceStatus;
import br.com.scheiner.aws.console.sqs.model.SqsExplorerQueue;
import br.com.scheiner.aws.console.sqs.service.SqsExplorerService;

class SqsResourceInfoProviderTest {

	@Test
	@DisplayName("Deve transformar filas SQS em recursos do dashboard")
	void deve_transformar_filas_sqs_em_recursos_do_dashboard() {
		var service = mock(SqsExplorerService.class);
		when(service.listarFilas()).thenReturn(List.of(
				new SqsExplorerQueue("fila-a", "url-a", "Standard", 1, false),
				new SqsExplorerQueue("fila-b", "url-b", "Standard", 0, false)));

		var info = new SqsResourceInfoProvider(service).load();

		assertThat(info.getType()).isEqualTo(ResourceType.SQS);
		assertThat(info.getStatus()).isEqualTo(ServiceStatus.CONNECTED);
		assertThat(info.getCount()).isEqualTo(2);
		assertThat(info.getResources()).extracting("name").containsExactly("fila-a", "fila-b");
		assertThat(info.getResources()).extracting("identifier").containsExactly("fila-a", "fila-b");
	}
}
