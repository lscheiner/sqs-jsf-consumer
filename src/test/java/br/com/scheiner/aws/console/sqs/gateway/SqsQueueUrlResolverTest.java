package br.com.scheiner.aws.console.sqs.gateway;



import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.com.scheiner.aws.console.configuration.aws.AwsConfiguration;

class SqsQueueUrlResolverTest {

	@Test
	@DisplayName("Deve montar URL da fila usando endpoint local e account id padrao")
	void deve_montar_url_da_fila_usando_endpoint_local_e_account_id_padrao() {
		var resolver = new SqsQueueUrlResolver(new AwsConfiguration("http://localhost:4566", "sa-east-1"));

		assertThat(resolver.montarQueueUrl("pedido-criado"))
				.isEqualTo("http://localhost:4566/000000000000/pedido-criado");
	}

	@Test
	@DisplayName("Deve extrair nome da fila a partir da URL")
	void deve_extrair_nome_da_fila_a_partir_da_url() {
		var resolver = new SqsQueueUrlResolver(new AwsConfiguration("http://localhost:4566", "sa-east-1"));

		assertThat(resolver.extrairNomeFila("http://localhost:4566/000000000000/pedido-criado"))
				.isEqualTo("pedido-criado");
	}
}
