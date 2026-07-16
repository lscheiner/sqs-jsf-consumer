package br.com.scheiner.aws.console.redis.model;



import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RedisModelTest {

	@Test
	@DisplayName("Deve armazenar dados do registro Redis")
	void deve_armazenar_dados_do_registro_redis() {
		var registro = new RedisRegistro();

		registro.setChave("chave");
		registro.setValor("valor");
		registro.setTipo("hash");
		registro.setTtl(10L);

		assertThat(registro.getChave()).isEqualTo("chave");
		assertThat(registro.getValor()).isEqualTo("valor");
		assertThat(registro.getTipo()).isEqualTo("hash");
		assertThat(registro.isEditavel()).isFalse();
		assertThat(registro.getTtl()).isEqualTo(10L);
	}

	@Test
	@DisplayName("Deve criar registro Redis com construtor completo")
	void deve_criar_registro_redis_com_construtor_completo() {
		var registro = new RedisRegistro("chave", "valor", -1L);

		assertThat(registro.getChave()).isEqualTo("chave");
		assertThat(registro.getValor()).isEqualTo("valor");
		assertThat(registro.getTtl()).isEqualTo(-1L);
		assertThat(registro.getTipo()).isEqualTo("string");
		assertThat(registro.isEditavel()).isTrue();
	}

	@Test
	@DisplayName("Deve armazenar configuracao Redis")
	void deve_armazenar_configuracao_redis() {
		var configuracao = new RedisConfiguracao();

		configuracao.setHost("localhost");
		configuracao.setPort(6379);
		configuracao.setTls(true);
		configuracao.setUsername("user");
		configuracao.setPassword("pass");

		assertThat(configuracao.getHost()).isEqualTo("localhost");
		assertThat(configuracao.getPort()).isEqualTo(6379);
		assertThat(configuracao.getTls()).isTrue();
		assertThat(configuracao.getUsername()).isEqualTo("user");
		assertThat(configuracao.getPassword()).isEqualTo("pass");
	}
}
