package br.com.scheiner.aws.console.redis.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import br.com.scheiner.aws.console.redis.config.RedisClientProvider;
import br.com.scheiner.aws.console.redis.config.RedisConnectionConfiguration;
import br.com.scheiner.aws.console.redis.model.RedisConfiguracao;
import br.com.scheiner.aws.console.redis.model.RedisRegistro;

@Service
public class RedisService {

	private static final String TODAS_CHAVES = "*";

	private final RedisClientProvider redisClientProvider;

	private final RedisConnectionConfiguration redisConnectionConfiguration;

	public RedisService(
			RedisClientProvider redisClientProvider,
			RedisConnectionConfiguration redisConnectionConfiguration) {
		this.redisClientProvider = redisClientProvider;
		this.redisConnectionConfiguration = redisConnectionConfiguration;
	}

	public List<RedisRegistro> listarRegistros() {
		var commands = this.redisClientProvider.getCommands();
		var chaves = new ArrayList<>(commands.keys(TODAS_CHAVES));

		chaves.sort(String::compareTo);

		return chaves.stream()
				.map(chave -> new RedisRegistro(chave, commands.get(chave), commands.ttl(chave)))
				.toList();
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
