package br.com.scheiner.aws.console.resource.model;



import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ResourceTypeTest {

	@Test
	@DisplayName("Deve expor nomes de exibicao dos tipos de recurso")
	void deve_expor_nomes_de_exibicao_dos_tipos_de_recurso() {
		assertThat(ResourceType.SQS.getDisplayName()).isEqualTo("SQS");
		assertThat(ResourceType.DYNAMODB.getDisplayName()).isEqualTo("DynamoDB");
		assertThat(ResourceType.REDIS.getDisplayName()).isEqualTo("Redis");
		assertThat(ResourceType.SNS.getDisplayName()).isEqualTo("SNS");
		assertThat(ResourceType.ROOT.getDisplayName()).isEqualTo("Resources");
	}
}
