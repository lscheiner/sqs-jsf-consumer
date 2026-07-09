package br.com.scheiner.aws.console.resource.model;



import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ResourceDescriptorTest {

	@Test
	@DisplayName("Deve armazenar tipo, nome e identificador do recurso")
	void deve_armazenar_tipo_nome_e_identificador_do_recurso() {
		var descriptor = new ResourceDescriptor();

		descriptor.setType(ResourceType.SNS);
		descriptor.setName("topico");
		descriptor.setIdentifier("arn");

		assertThat(descriptor.getType()).isEqualTo(ResourceType.SNS);
		assertThat(descriptor.getName()).isEqualTo("topico");
		assertThat(descriptor.getIdentifier()).isEqualTo("arn");
	}
}
