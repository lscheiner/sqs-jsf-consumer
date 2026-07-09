package br.com.scheiner.aws.console.web.navigation;



import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.com.scheiner.aws.console.resource.model.ResourceType;

class ApplicationRouteTest {

	@Test
	@DisplayName("Deve montar rota com parametro codificado quando o recurso for informado")
	void deve_montar_rota_com_parametro_codificado_quando_recurso_for_informado() {
		assertThat(ApplicationRoute.SQS.getPath("fila com espaco"))
				.isEqualTo("/sqs-explorer.xhtml?fila=fila+com+espaco");
	}

	@Test
	@DisplayName("Deve retornar rota base quando nao houver parametro de recurso")
	void deve_retornar_rota_base_quando_nao_houver_parametro_de_recurso() {
		assertThat(ApplicationRoute.REDIS.getPath("qualquer")).isEqualTo("/redis.xhtml");
		assertThat(ApplicationRoute.SNS.getPath("")).isEqualTo("/sns.xhtml");
	}

	@Test
	@DisplayName("Deve resolver a rota correspondente ao tipo de recurso")
	void deve_resolver_rota_correspondente_ao_tipo_de_recurso() {
		assertThat(ApplicationRoute.from(ResourceType.SQS)).isEqualTo(ApplicationRoute.SQS);
		assertThat(ApplicationRoute.from(ResourceType.SNS)).isEqualTo(ApplicationRoute.SNS);
		assertThat(ApplicationRoute.from(ResourceType.DYNAMODB)).isEqualTo(ApplicationRoute.DYNAMODB);
		assertThat(ApplicationRoute.from(ResourceType.REDIS)).isEqualTo(ApplicationRoute.REDIS);
		assertThat(ApplicationRoute.from(ResourceType.ROOT)).isNull();
	}
}
