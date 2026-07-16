package br.com.scheiner.aws.console.redis.service;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.scheiner.aws.console.redis.config.RedisClientProvider;
import br.com.scheiner.aws.console.redis.config.RedisConnectionConfiguration;
import br.com.scheiner.aws.console.redis.model.RedisConfiguracao;
import br.com.scheiner.aws.console.redis.model.RedisRegistro;

@Service
public class RedisService {

	private static final String TODAS_CHAVES = "*";
	private final RedisClientProvider redisClientProvider;

	private final RedisConnectionConfiguration redisConnectionConfiguration;

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	public RedisService(
			RedisClientProvider redisClientProvider,
			RedisConnectionConfiguration redisConnectionConfiguration
			) {
		this.redisClientProvider = redisClientProvider;
		this.redisConnectionConfiguration = redisConnectionConfiguration;
	}

	public List<RedisRegistro> listarRegistros() {
		var commands = this.redisClientProvider.getCommands();
		var chaves = new ArrayList<>(commands.keys(TODAS_CHAVES));

		chaves.sort(String::compareTo);

		return chaves.stream()
				.map(chave -> this.criarRegistro(commands, chave))
				.toList();
	}

	private RedisRegistro criarRegistro(io.lettuce.core.api.sync.RedisCommands<String, String> commands, String chave) {
		var tipo = commands.type(chave);
		return new RedisRegistro(chave, this.lerValor(commands, chave, tipo), tipo, commands.ttl(chave));
	}

	private String lerValor(io.lettuce.core.api.sync.RedisCommands<String, String> commands, String chave, String tipo) {
		return switch (tipo) {
			case "string" -> commands.get(chave);
			case "hash" -> this.paraJson(commands.hgetall(chave));
			case "list" -> this.paraJson(commands.lrange(chave, 0, -1));
			case "set" -> this.paraJson(commands.smembers(chave));
			case "zset" -> this.paraJson(commands.zrangeWithScores(chave, 0, -1));
			case "stream" -> "[%d entradas]".formatted(commands.xlen(chave));
			default -> "[%s]".formatted(tipo);
		};
	}

	private String paraJson(Object valor) {
		try {
			return OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(valor);
		} catch (JsonProcessingException exception) {
			throw new IllegalStateException("Nao foi possivel formatar o valor Redis.", exception);
		}
	}

	public long contarChaves() {
		return this.redisClientProvider.getCommands().dbsize();
	}

	public void salvarRegistro(String chave, String valor, Long ttl) {
		var commands = this.redisClientProvider.getCommands();

		if (ttl != null && ttl > 0) {
			commands.setex(chave, ttl, valor);
			return;
		}

		commands.set(chave, valor);
	}

	public void excluirRegistro(String chave) {
		this.redisClientProvider.getCommands().del(chave);
	}

	public boolean testarConexao() {
		return this.redisClientProvider.isConectado();
	}

	public RedisConfiguracao carregarConfiguracao() {
		return new RedisConfiguracao(
				this.redisConnectionConfiguration.getHost(),
				this.redisConnectionConfiguration.getPort(),
				this.redisConnectionConfiguration.getTls(),
				this.redisConnectionConfiguration.getUsername(),
				this.redisConnectionConfiguration.getPassword());
	}

	public void aplicarConfiguracao(RedisConfiguracao configuracao) {
		this.redisConnectionConfiguration.setHost(configuracao.getHost());
		this.redisConnectionConfiguration.setPort(configuracao.getPort());
		this.redisConnectionConfiguration.setTls(configuracao.getTls());
		this.redisConnectionConfiguration.setUsername(configuracao.getUsername());
		this.redisConnectionConfiguration.setPassword(configuracao.getPassword());
		this.redisClientProvider.reconfigurar();
	}
}
