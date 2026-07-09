package br.com.scheiner.aws.console.redis.controller;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.com.scheiner.aws.console.redis.service.RedisService;

class RedisControllerTest {

	private final RedisController controller = new RedisController(mock(RedisService.class));

	@Test
	@DisplayName("Deve formatar TTL nulo, sem expiracao, expirado e positivo")
	void deve_formatar_ttl_nulo_sem_expiracao_expirado_e_positivo() {
		assertThat(this.controller.formatarTtl(null)).isEqualTo("-");
		assertThat(this.controller.formatarTtl(-1L)).isEqualTo("Sem expiração");
		assertThat(this.controller.formatarTtl(-2L)).isEqualTo("Expirado");
		assertThat(this.controller.formatarTtl(30L)).isEqualTo("30 segundos");
	}

	@Test
	@DisplayName("Deve gerar preview vazio para valor nulo ou em branco")
	void deve_gerar_preview_vazio_para_valor_nulo_ou_em_branco() {
		assertThat(this.controller.previewValor(null)).isEmpty();
		assertThat(this.controller.previewValor("   ")).isEmpty();
	}

	@Test
	@DisplayName("Deve normalizar espacos e limitar preview de valores longos")
	void deve_normalizar_espacos_e_limitar_preview_de_valores_longos() {
		var valorLongo = "a".repeat(130);

		assertThat(this.controller.previewValor("valor\n com\t espacos")).isEqualTo("valor com espacos");
		assertThat(this.controller.previewValor(valorLongo))
				.hasSize(123)
				.endsWith("...");
	}
}
