package br.com.scheiner.aws.console.sns.model;



import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SnsModelTest {

	@Test
	@DisplayName("Deve armazenar dados do topico SNS")
	void deve_armazenar_dados_do_topico_sns() {
		var topico = new SnsTopic();

		topico.setNome("topico");
		topico.setArn("arn");
		topico.setQuantidadeAssinaturas(2);

		assertThat(topico.getNome()).isEqualTo("topico");
		assertThat(topico.getArn()).isEqualTo("arn");
		assertThat(topico.getQuantidadeAssinaturas()).isEqualTo(2);
	}

	@Test
	@DisplayName("Deve criar topico SNS com construtor completo")
	void deve_criar_topico_sns_com_construtor_completo() {
		var topico = new SnsTopic("topico", "arn", 1);

		assertThat(topico.getNome()).isEqualTo("topico");
		assertThat(topico.getArn()).isEqualTo("arn");
		assertThat(topico.getQuantidadeAssinaturas()).isEqualTo(1);
	}

	@Test
	@DisplayName("Deve armazenar dados da assinatura SNS")
	void deve_armazenar_dados_da_assinatura_sns() {
		var assinatura = new SnsSubscription();

		assinatura.setArn("arn");
		assinatura.setProtocolo("sqs");
		assinatura.setEndpoint("fila");

		assertThat(assinatura.getArn()).isEqualTo("arn");
		assertThat(assinatura.getProtocolo()).isEqualTo("sqs");
		assertThat(assinatura.getEndpoint()).isEqualTo("fila");
	}

	@Test
	@DisplayName("Deve criar assinatura SNS com construtor completo")
	void deve_criar_assinatura_sns_com_construtor_completo() {
		var assinatura = new SnsSubscription("arn", "sqs", "fila");

		assertThat(assinatura.getArn()).isEqualTo("arn");
		assertThat(assinatura.getProtocolo()).isEqualTo("sqs");
		assertThat(assinatura.getEndpoint()).isEqualTo("fila");
	}
}
