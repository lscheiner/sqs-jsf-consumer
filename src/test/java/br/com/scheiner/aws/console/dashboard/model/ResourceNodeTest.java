package br.com.scheiner.aws.console.dashboard.model;


import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.com.scheiner.aws.console.resource.model.ResourceType;

class ResourceNodeTest {

	@Test
	@DisplayName("Deve criar grupo usando o nome de exibicao do tipo")
	void deve_criar_grupo_usando_o_nome_de_exibicao_do_tipo() {
		var node = ResourceNode.group(ResourceType.DYNAMODB);

		assertThat(node.getType()).isEqualTo(ResourceType.DYNAMODB);
		assertThat(node.getName()).isEqualTo("DynamoDB");
		assertThat(node.getIdentifier()).isNull();
		assertThat(node.isResource()).isFalse();
		assertThat(node).hasToString("DynamoDB");
	}

	@Test
	@DisplayName("Deve criar recurso navegavel com identificador")
	void deve_criar_recurso_navegavel_com_identificador() {
		var node = ResourceNode.resource(ResourceType.SQS, "fila", "url");

		assertThat(node.getType()).isEqualTo(ResourceType.SQS);
		assertThat(node.getName()).isEqualTo("fila");
		assertThat(node.getIdentifier()).isEqualTo("url");
		assertThat(node.isResource()).isTrue();
	}

	@Test
	@DisplayName("Deve permitir preencher node pelo construtor padrao")
	void deve_permitir_preencher_node_pelo_construtor_padrao() {
		var node = new ResourceNode();

		node.setType(ResourceType.REDIS);
		node.setName("redis");
		node.setIdentifier("redis");
		node.setResource(true);

		assertThat(node.getType()).isEqualTo(ResourceType.REDIS);
		assertThat(node.getName()).isEqualTo("redis");
		assertThat(node.getIdentifier()).isEqualTo("redis");
		assertThat(node.isResource()).isTrue();
	}
}
