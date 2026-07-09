package br.com.scheiner.aws.console.dashboard.model;


import static org.assertj.core.api.Assertions.assertThat;

import java.util.EnumMap;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.com.scheiner.aws.console.resource.model.ResourceType;

class DashboardDataTest {

	@Test
	@DisplayName("Deve retornar lista vazia quando nao houver recursos para o tipo")
	void deve_retornar_lista_vazia_quando_nao_houver_recursos_para_o_tipo() {
		assertThat(new DashboardData().getResources(ResourceType.SQS)).isEmpty();
	}

	@Test
	@DisplayName("Deve retornar recursos cadastrados para o tipo informado")
	void deve_retornar_recursos_cadastrados_para_o_tipo_informado() {
		var data = new DashboardData();
		var recursos = new EnumMap<ResourceType, List<ResourceNode>>(ResourceType.class);
		var fila = ResourceNode.resource(ResourceType.SQS, "fila", "url");
		recursos.put(ResourceType.SQS, List.of(fila));
		data.setResources(recursos);

		assertThat(data.getResources(ResourceType.SQS)).containsExactly(fila);
		assertThat(data.getResources()).isSameAs(recursos);
	}
}
