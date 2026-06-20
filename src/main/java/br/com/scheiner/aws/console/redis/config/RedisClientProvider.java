package br.com.scheiner.aws.console.redis.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;

@Component
public class RedisClientProvider {

	private static final Logger LOGGER = LoggerFactory.getLogger(RedisClientProvider.class);

	private final RedisConnectionConfiguration redisConnectionConfiguration;

	private RedisClient client;

	private StatefulRedisConnection<String, String> connection;

	public RedisClientProvider(RedisConnectionConfiguration redisConnectionConfiguration) {
		this.redisConnectionConfiguration = redisConnectionConfiguration;
		this.reconfigurar();
	}

	public void reconfigurar() {
		this.fecharConexaoAtual();

		var redisUri = this.criarRedisUri();
		this.client = RedisClient.create(redisUri);
	}

	public boolean isConectado() {
		try {
			this.getCommands().ping();
			return true;
		} catch (Exception e) {
			LOGGER.error("Erro conectando no Redis", e);
			return false;
		}
	}

	public RedisCommands<String, String> getCommands() {
		if (this.connection == null || !this.connection.isOpen()) {
			this.connection = this.client.connect();
		}

		return this.connection.sync();
	}

	private RedisURI criarRedisUri() {
		var builder = RedisURI.builder()
				.withHost(this.redisConnectionConfiguration.getHost())
				.withPort(this.redisConnectionConfiguration.getPort())
				.withSsl(Boolean.TRUE.equals(this.redisConnectionConfiguration.getTls()));

		var username = this.redisConnectionConfiguration.getUsername();
		var password = this.redisConnectionConfiguration.getPassword();

		if (StringUtils.hasText(username) && StringUtils.hasText(password)) {
			builder.withAuthentication(username, password.toCharArray());
		} else if (StringUtils.hasText(password)) {
			builder.withPassword(password.toCharArray());
		}

		return builder.build();
	}

	private void fecharConexaoAtual() {
		if (this.connection != null) {
			this.connection.close();
			this.connection = null;
		}

		if (this.client != null) {
			this.client.shutdown();
			this.client = null;
		}
	}
}
