package br.com.scheiner.aws.console.redis.summary;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.com.scheiner.aws.console.redis.model.RedisConfiguracao;
import br.com.scheiner.aws.console.redis.service.RedisService;
import br.com.scheiner.aws.console.resource.model.ResourceType;
import br.com.scheiner.aws.console.resource.model.ServiceStatus;

class RedisResourceInfoProviderTest {

	@Test
	@DisplayName("Deve informar Redis conectado com quantidade de chaves e endereco configurado")
	void deve_informar_redis_conectado_com_quantidade_de_chaves_e_endereco_configurado() {
		var service = mock(RedisService.class);
		when(service.carregarConfiguracao()).thenReturn(new RedisConfiguracao("localhost", 6379, false, "", ""));
		when(service.testarConexao()).thenReturn(true);
		when(service.contarChaves()).thenReturn(7L);

		var info = new RedisResourceInfoProvider(service).load();

		assertThat(info.getType()).isEqualTo(ResourceType.REDIS);
		assertThat(info.getStatus()).isEqualTo(ServiceStatus.CONNECTED);
		assertThat(info.getConfiguredAddress()).isEqualTo("localhost:6379");
		assertThat(info.getCount()).isEqualTo(7L);
		var recurso = info.getResources().getFirst();
		assertThat(info.getResources()).hasSize(1);
		assertThat(recurso.getName()).isEqualTo("localhost:6379");
		assertThat(recurso.getIdentifier()).isEqualTo("localhost:6379");
	}

	@Test
	@DisplayName("Deve informar Redis desconectado sem contar chaves")
	void deve_informar_redis_desconectado_sem_contar_chaves() {
		var service = mock(RedisService.class);
		when(service.carregarConfiguracao()).thenReturn(new RedisConfiguracao("redis", 6380, true, "user", "pass"));
		when(service.testarConexao()).thenReturn(false);

		var info = new RedisResourceInfoProvider(service).load();

		assertThat(info.getStatus()).isEqualTo(ServiceStatus.DISCONNECTED);
		assertThat(info.getConfiguredAddress()).isEqualTo("redis:6380");
		assertThat(info.getCount()).isZero();
	}
}
