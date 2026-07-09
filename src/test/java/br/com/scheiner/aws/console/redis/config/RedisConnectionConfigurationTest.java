package br.com.scheiner.aws.console.redis.config;



import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RedisConnectionConfigurationTest {

	@Test
	@DisplayName("Deve manter configuracao Redis e permitir alteracao em runtime")
	void deve_manter_configuracao_redis_e_permitir_alteracao_em_runtime() {
		var configuracao = new RedisConnectionConfiguration("localhost", 6379, false, "", "");

		configuracao.setHost("redis.local");
		configuracao.setPort(6380);
		configuracao.setTls(true);
		configuracao.setUsername("usuario");
		configuracao.setPassword("senha");

		assertThat(configuracao.getHost()).isEqualTo("redis.local");
		assertThat(configuracao.getPort()).isEqualTo(6380);
		assertThat(configuracao.getTls()).isTrue();
		assertThat(configuracao.getUsername()).isEqualTo("usuario");
		assertThat(configuracao.getPassword()).isEqualTo("senha");
	}
}
