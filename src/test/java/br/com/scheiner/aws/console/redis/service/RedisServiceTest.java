package br.com.scheiner.aws.console.redis.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.com.scheiner.aws.console.redis.config.RedisClientProvider;
import br.com.scheiner.aws.console.redis.config.RedisConnectionConfiguration;
import br.com.scheiner.aws.console.redis.model.RedisConfiguracao;
import io.lettuce.core.ScoredValue;
import io.lettuce.core.api.sync.RedisCommands;

class RedisServiceTest {

	private final RedisClientProvider provider = mock(RedisClientProvider.class);
	private final RedisConnectionConfiguration configuracao =
			new RedisConnectionConfiguration("localhost", 6379, false, "", "");
	private final RedisService service = new RedisService(this.provider, this.configuracao);

	@Test
	@DisplayName("Deve listar registros Redis ordenados pela chave")
	void deve_listar_registros_redis_ordenados_pela_chave() {
		var commands = commands();
		when(this.provider.getCommands()).thenReturn(commands);
		when(commands.keys("*")).thenReturn(List.of("b", "a"));
		when(commands.type("a")).thenReturn("string");
		when(commands.type("b")).thenReturn("string");
		when(commands.get("a")).thenReturn("valor-a");
		when(commands.get("b")).thenReturn("valor-b");
		when(commands.ttl("a")).thenReturn(10L);
		when(commands.ttl("b")).thenReturn(-1L);

		var registros = this.service.listarRegistros();

		assertThat(registros).extracting("chave").containsExactly("a", "b");
		assertThat(registros).extracting("valor").containsExactly("valor-a", "valor-b");
		assertThat(registros).extracting("tipo").containsExactly("string", "string");
		assertThat(registros).extracting("ttl").containsExactly(10L, -1L);
	}

	@Test
	@DisplayName("Deve listar todos os tipos Redis sem executar GET para chaves nao-string")
	void deve_listar_todos_os_tipos_redis_sem_executar_get_para_chaves_nao_string() {
		var commands = commands();
		when(this.provider.getCommands()).thenReturn(commands);
		when(commands.keys("*")).thenReturn(List.of("usuario:1", "fila", "tags", "ranking", "eventos"));
		when(commands.type("usuario:1")).thenReturn("hash");
		when(commands.type("fila")).thenReturn("list");
		when(commands.type("tags")).thenReturn("set");
		when(commands.type("ranking")).thenReturn("zset");
		when(commands.type("eventos")).thenReturn("stream");
		when(commands.hgetall("usuario:1")).thenReturn(java.util.Map.of("nome", "Leandro"));
		when(commands.lrange("fila", 0, -1)).thenReturn(List.of("primeiro", "segundo"));
		when(commands.smembers("tags")).thenReturn(java.util.Set.of("localstack"));
		when(commands.zrangeWithScores("ranking", 0, -1)).thenReturn(List.of(ScoredValue.just(10.0, "Leandro")));
		when(commands.xlen("eventos")).thenReturn(3L);
		when(commands.ttl("usuario:1")).thenReturn(-1L);
		when(commands.ttl("fila")).thenReturn(60L);
		when(commands.ttl("tags")).thenReturn(-1L);
		when(commands.ttl("ranking")).thenReturn(-1L);
		when(commands.ttl("eventos")).thenReturn(-1L);

		var registros = this.service.listarRegistros();

		assertThat(registros).extracting("tipo").containsExactly("stream", "list", "zset", "set", "hash");
		assertThat(registros).allMatch(registro -> !registro.isEditavel());
		assertThat(registros).filteredOn(registro -> registro.getChave().equals("eventos"))
				.extracting("valor").containsExactly("[3 entradas]");
		verify(commands, never()).get("usuario:1");
		verify(commands, never()).get("fila");
	}

	@Test
	@DisplayName("Deve exibir tipos Redis desconhecidos sem falhar")
	void deve_exibir_tipos_redis_desconhecidos_sem_falhar() {
		var commands = commands();
		when(this.provider.getCommands()).thenReturn(commands);
		when(commands.keys("*")).thenReturn(List.of("indice"));
		when(commands.type("indice")).thenReturn("ReJSON-RL");
		when(commands.ttl("indice")).thenReturn(-1L);

		var registros = this.service.listarRegistros();

		assertThat(registros).singleElement()
				.satisfies(registro -> {
					assertThat(registro.getTipo()).isEqualTo("ReJSON-RL");
					assertThat(registro.getValor()).isEqualTo("[ReJSON-RL]");
				});
		verify(commands, never()).get("indice");
	}

	@Test
	@DisplayName("Deve informar falha ao formatar valor Redis em JSON")
	@SuppressWarnings({ "rawtypes", "unchecked" })
	void deve_informar_falha_ao_formatar_valor_redis_em_json() {
		var commands = commands();
		Map valorCircular = new java.util.HashMap<>();
		valorCircular.put("self", valorCircular);
		when(this.provider.getCommands()).thenReturn(commands);
		when(commands.keys("*")).thenReturn(List.of("usuario:1"));
		when(commands.type("usuario:1")).thenReturn("hash");
		when(commands.hgetall("usuario:1")).thenReturn(valorCircular);

		assertThatThrownBy(this.service::listarRegistros)
				.isInstanceOf(IllegalStateException.class)
				.hasMessage("Nao foi possivel formatar o valor Redis.");
	}

	@Test
	@DisplayName("Deve contar chaves usando tamanho do banco Redis")
	void deve_contar_chaves_usando_tamanho_do_banco_redis() {
		var commands = commands();
		when(this.provider.getCommands()).thenReturn(commands);
		when(commands.dbsize()).thenReturn(5L);

		assertThat(this.service.contarChaves()).isEqualTo(5L);
	}

	@Test
	@DisplayName("Deve salvar registro sem expiracao quando TTL nao for positivo")
	void deve_salvar_registro_sem_expiracao_quando_ttl_nao_for_positivo() {
		var commands = commands();
		when(this.provider.getCommands()).thenReturn(commands);
		when(commands.set("chave", "valor")).thenReturn("OK");

		this.service.salvarRegistro("chave", "valor", 0L);

		verify(commands).set("chave", "valor");
		verify(commands, never()).setex("chave", 0L, "valor");
	}

	@Test
	@DisplayName("Deve salvar registro com expiracao quando TTL for positivo")
	void deve_salvar_registro_com_expiracao_quando_ttl_for_positivo() {
		var commands = commands();
		when(this.provider.getCommands()).thenReturn(commands);
		when(commands.setex("chave", 60L, "valor")).thenReturn("OK");

		this.service.salvarRegistro("chave", "valor", 60L);

		verify(commands).setex("chave", 60L, "valor");
		verify(commands, never()).set("chave", "valor");
	}

	@Test
	@DisplayName("Deve excluir registro pela chave")
	void deve_excluir_registro_pela_chave() {
		var commands = commands();
		when(this.provider.getCommands()).thenReturn(commands);
		when(commands.del("chave")).thenReturn(1L);

		this.service.excluirRegistro("chave");

		verify(commands).del("chave");
	}

	@Test
	@DisplayName("Deve retornar resultado do teste de conexao do provider")
	void deve_retornar_resultado_do_teste_de_conexao_do_provider() {
		when(this.provider.isConectado()).thenReturn(true);

		assertThat(this.service.testarConexao()).isTrue();
	}

	@Test
	@DisplayName("Deve carregar configuracao atual do Redis")
	void deve_carregar_configuracao_atual_do_redis() {
		var atual = this.service.carregarConfiguracao();

		assertThat(atual.getHost()).isEqualTo("localhost");
		assertThat(atual.getPort()).isEqualTo(6379);
		assertThat(atual.getTls()).isFalse();
		assertThat(atual.getUsername()).isEmpty();
		assertThat(atual.getPassword()).isEmpty();
	}

	@Test
	@DisplayName("Deve aplicar configuracao Redis em runtime")
	void deve_aplicar_configuracao_redis_em_runtime() {
		this.service.aplicarConfiguracao(
				new RedisConfiguracao("redis.local", 6380, true, "user", "pass"));

		var atual = this.service.carregarConfiguracao();

		assertThat(atual.getHost()).isEqualTo("redis.local");
		assertThat(atual.getPort()).isEqualTo(6380);
		assertThat(atual.getTls()).isTrue();
		assertThat(atual.getUsername()).isEqualTo("user");
		assertThat(atual.getPassword()).isEqualTo("pass");
	}
	
	@Test
	@DisplayName("Deve salvar registro sem expiracao quando TTL for nulo")
	void deve_salvar_registro_sem_expiracao_quando_ttl_for_nulo() {
		var commands = commands();

		when(this.provider.getCommands()).thenReturn(commands);
		when(commands.set("chave", "valor")).thenReturn("OK");

		this.service.salvarRegistro("chave", "valor", null);

		verify(commands).set("chave", "valor");
		verify(commands, never()).setex("chave", 60L, "valor");
	}

	@SuppressWarnings("unchecked")
	private static RedisCommands<String, String> commands() {
		return mock(RedisCommands.class);
	}
}
