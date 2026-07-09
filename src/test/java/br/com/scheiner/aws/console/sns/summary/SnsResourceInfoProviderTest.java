package br.com.scheiner.aws.console.sns.summary;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.com.scheiner.aws.console.resource.model.ResourceType;
import br.com.scheiner.aws.console.resource.model.ServiceStatus;
import br.com.scheiner.aws.console.sns.model.SnsTopic;
import br.com.scheiner.aws.console.sns.service.SnsService;

class SnsResourceInfoProviderTest {

	@Test
	@DisplayName("Deve transformar topicos SNS em recursos do dashboard")
	void deve_transformar_topicos_sns_em_recursos_do_dashboard() {
		var service = mock(SnsService.class);
		when(service.listarTopicos()).thenReturn(List.of(new SnsTopic("topico", "arn:topico", 1)));

		var info = new SnsResourceInfoProvider(service).load();

		assertThat(info.getType()).isEqualTo(ResourceType.SNS);
		assertThat(info.getStatus()).isEqualTo(ServiceStatus.CONNECTED);
		assertThat(info.getCount()).isEqualTo(1);
		var recurso = info.getResources().getFirst();
		assertThat(info.getResources()).hasSize(1);
		assertThat(recurso.getName()).isEqualTo("topico");
		assertThat(recurso.getIdentifier()).isEqualTo("arn:topico");
	}
}
