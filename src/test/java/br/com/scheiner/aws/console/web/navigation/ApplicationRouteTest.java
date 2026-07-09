package br.com.scheiner.aws.console.web.navigation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.com.scheiner.aws.console.resource.model.ResourceType;

class ApplicationRouteTest {

	@Test
	@DisplayName("Deve retornar o caminho configurado para cada rota")
	void deve_retornar_o_caminho_configurado_para_cada_rota() {
		assertThat(ApplicationRoute.DASHBOARD.getPath()).isEqualTo("/dashboard.xhtml");
		assertThat(ApplicationRoute.SQS.getPath()).isEqualTo("/sqs-explorer.xhtml");
		assertThat(ApplicationRoute.SNS.getPath()).isEqualTo("/sns.xhtml");
		assertThat(ApplicationRoute.DYNAMODB.getPath()).isEqualTo("/dynamodb.xhtml");
		assertThat(ApplicationRoute.REDIS.getPath()).isEqualTo("/redis.xhtml");
	}

	@Test
	@DisplayName("Deve retornar o parametro de recurso configurado para cada rota")
	void deve_retornar_o_parametro_de_recurso_configurado_para_cada_rota() {
		assertThat(ApplicationRoute.DASHBOARD.getResourceParameter()).isNull();
		assertThat(ApplicationRoute.SQS.getResourceParameter()).isEqualTo("fila");
		assertThat(ApplicationRoute.SNS.getResourceParameter()).isEqualTo("topico");
		assertThat(ApplicationRoute.DYNAMODB.getResourceParameter()).isEqualTo("tabela");
		assertThat(ApplicationRoute.REDIS.getResourceParameter()).isNull();
	}

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
		assertThat(ApplicationRoute.DASHBOARD.getPath("qualquer")).isEqualTo("/dashboard.xhtml");
		assertThat(ApplicationRoute.SNS.getPath("")).isEqualTo("/sns.xhtml");
		assertThat(ApplicationRoute.SNS.getPath("   ")).isEqualTo("/sns.xhtml");
		assertThat(ApplicationRoute.SNS.getPath((String) null)).isEqualTo("/sns.xhtml");
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