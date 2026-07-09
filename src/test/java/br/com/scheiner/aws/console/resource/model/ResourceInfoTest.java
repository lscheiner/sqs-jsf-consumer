package br.com.scheiner.aws.console.resource.model;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ResourceInfoTest {

	@Test
	@DisplayName("Deve expor dados configurados do recurso")
	void deve_expor_dados_configurados_do_recurso() {
		var descriptor = new ResourceDescriptor(ResourceType.SQS, "fila", "url");
		var info = new ResourceInfo();

		info.setType(ResourceType.SQS);
		info.setStatus(ServiceStatus.CONNECTED);
		info.setCount(2);
		info.setConfiguredAddress("localhost");
		info.setResources(List.of(descriptor));

		assertThat(info.getType()).isEqualTo(ResourceType.SQS);
		assertThat(info.getStatus()).isEqualTo(ServiceStatus.CONNECTED);
		assertThat(info.getCount()).isEqualTo(2);
		assertThat(info.getCountAsInt()).isEqualTo(2);
		assertThat(info.getConfiguredAddress()).isEqualTo("localhost");
		assertThat(info.getResources()).containsExactly(descriptor);
	}

	@Test
	@DisplayName("Deve falhar ao converter contagem para inteiro quando valor excede o limite")
	void deve_falhar_ao_converter_contagem_para_inteiro_quando_valor_excede_o_limite() {
		var info = new ResourceInfo();
		info.setCount(Long.MAX_VALUE);

		assertThrows(ArithmeticException.class, info::getCountAsInt);
	}
}
